package com.careround.onboarding.service;

import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Hospital;
import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.repository.HospitalRepository;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.onboarding.dto.CreateHospitalOnboardingRequest;
import com.careround.onboarding.dto.HospitalOnboardingResponse;
import com.careround.onboarding.dto.ProvisionHospitalResponse;
import com.careround.onboarding.dto.ProvisionHospitalTenantRequest;
import com.careround.onboarding.dto.ReviewHospitalOnboardingRequest;
import com.careround.onboarding.entity.ActivationToken;
import com.careround.onboarding.entity.HospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import com.careround.onboarding.repository.ActivationTokenRepository;
import com.careround.onboarding.repository.HospitalOnboardingRequestRepository;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ConflictException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalOnboardingServiceImpl implements HospitalOnboardingService {

    private static final String SENTINEL_HOSPITAL_ID = "00000000-0000-0000-0000-000000000000";
    private static final Set<HospitalOnboardingStatus> REVIEWABLE_STATUSES = Set.of(
            HospitalOnboardingStatus.CONTACTED,
            HospitalOnboardingStatus.APPROVED,
            HospitalOnboardingStatus.REJECTED
    );

    private final HospitalOnboardingRequestRepository onboardingRequestRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final HospitalRepository hospitalRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public HospitalOnboardingResponse submitRequest(CreateHospitalOnboardingRequest request) {
        if (onboardingRequestRepository.existsByContactEmailAndStatus(
                request.contactEmail(), HospitalOnboardingStatus.PENDING_REVIEW)) {
            throw new ConflictException(
                    "An onboarding request for this contact email is already pending.");
        }

        HospitalOnboardingRequest entity = new HospitalOnboardingRequest();
        entity.setHospitalName(request.hospitalName());
        entity.setCountryOrRegion(request.countryOrRegion());
        entity.setContactEmail(request.contactEmail());
        entity.setContactPhone(request.contactPhone());
        entity.setHospitalType(request.hospitalType());
        entity.setEstimatedBeds(request.estimatedInpatientBeds());
        entity.setPrimaryNeed(request.primaryNeed());
        entity.setStatus(HospitalOnboardingStatus.PENDING_REVIEW);
        entity = onboardingRequestRepository.save(entity);

        outboxService.publish("hospital-onboarding-requested",
                Map.of("requestId", entity.getId(), "contactEmail", entity.getContactEmail()),
                SENTINEL_HOSPITAL_ID);

        log.info("action=ONBOARDING_REQUEST_SUBMITTED requestId={} email={}",
                entity.getId(), entity.getContactEmail());
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HospitalOnboardingResponse> listRequests(HospitalOnboardingStatus status, Pageable pageable) {
        if (status != null) {
            return onboardingRequestRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return onboardingRequestRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalOnboardingResponse getRequest(String requestId) {
        return toResponse(findOrThrow(requestId));
    }

    @Override
    @Transactional
    public HospitalOnboardingResponse reviewRequest(String requestId, String reviewerUserId,
                                                     ReviewHospitalOnboardingRequest request) {
        if (!REVIEWABLE_STATUSES.contains(request.status())) {
            throw new BusinessRuleException(
                    "Invalid review status. Allowed: CONTACTED, APPROVED, REJECTED");
        }

        HospitalOnboardingRequest entity = findOrThrow(requestId);

        if (entity.getStatus() == HospitalOnboardingStatus.PROVISIONED) {
            throw new BusinessRuleException("Cannot review a provisioned request.");
        }

        entity.setStatus(request.status());
        entity.setReviewNotes(request.reviewNotes());
        entity.setReviewedByUserId(reviewerUserId);
        entity.setReviewedAt(LocalDateTime.now(ZoneOffset.UTC));

        outboxService.publish("hospital-onboarding-reviewed",
                Map.of("requestId", entity.getId(), "status", entity.getStatus().name()),
                SENTINEL_HOSPITAL_ID);

        log.info("action=ONBOARDING_REQUEST_REVIEWED requestId={} status={} reviewedBy={}",
                entity.getId(), entity.getStatus(), reviewerUserId);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public ProvisionHospitalResponse provisionTenant(String requestId,
                                                      ProvisionHospitalTenantRequest request) {
        HospitalOnboardingRequest onboardingRequest = findOrThrow(requestId);

        if (onboardingRequest.getStatus() == HospitalOnboardingStatus.PROVISIONED) {
            throw new ConflictException("This onboarding request has already been provisioned.");
        }
        if (onboardingRequest.getStatus() != HospitalOnboardingStatus.APPROVED) {
            throw new BusinessRuleException("Only approved onboarding requests can be provisioned.");
        }

        if (hospitalRepository.existsByContactEmail(request.contactEmail())) {
            throw new ConflictException("A hospital with this contact email already exists.");
        }

        String code = generateCode(request.hospitalName());
        if (hospitalRepository.existsByCode(code)) {
            throw new ConflictException(
                    "Generated hospital code '" + code + "' already exists. Adjust the hospital name.");
        }

        // Create Hospital
        Hospital hospital = new Hospital();
        hospital.setName(request.hospitalName());
        hospital.setCode(code);
        hospital.setAddress(request.address());
        hospital.setContactEmail(request.contactEmail());
        hospital.setContactPhone(request.contactPhone());
        hospital = hospitalRepository.save(hospital);

        // Create SystemConfiguration
        SystemConfiguration config = new SystemConfiguration();
        config.setHospitalId(hospital.getId());
        config.setTaskOverdueReminderMinutes(request.taskOverdueGraceMinutes());
        config.setPushNotificationsEnabled(request.roundNotificationsEnabled());
        systemConfigurationRepository.save(config);

        // Create first ADMIN user (inactive — activation required)
        User admin = new User();
        admin.setHospitalId(hospital.getId());
        admin.setFirstName(request.adminFirstName());
        admin.setLastName(request.adminLastName());
        admin.setEmail(request.adminEmail());
        admin.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(false);
        admin = userRepository.save(admin);

        // Generate activation token (stored as SHA-256 hash)
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawToken);
        ActivationToken activationToken = new ActivationToken();
        activationToken.setUserId(admin.getId());
        activationToken.setTokenHash(tokenHash);
        activationToken.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(48));
        activationTokenRepository.save(activationToken);

        // Mark onboarding request as provisioned
        onboardingRequest.setStatus(HospitalOnboardingStatus.PROVISIONED);
        onboardingRequest.setProvisionedHospitalId(hospital.getId());

        outboxService.publish("hospital-provisioned",
                Map.of("requestId", requestId, "hospitalId", hospital.getId(),
                        "adminUserId", admin.getId()),
                hospital.getId());

        outboxService.publish("user-activation-requested",
                Map.of("hospitalId", hospital.getId(),
                        "userId", admin.getId(),
                        "email", admin.getEmail(),
                        "activationToken", rawToken),
                hospital.getId());

        log.info("action=HOSPITAL_PROVISIONED requestId={} hospitalId={} adminUserId={}",
                requestId, hospital.getId(), admin.getId());

        return new ProvisionHospitalResponse(requestId, hospital.getId(), admin.getId(),
                HospitalOnboardingStatus.PROVISIONED);
    }

    private HospitalOnboardingRequest findOrThrow(String requestId) {
        return onboardingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));
    }

    private HospitalOnboardingResponse toResponse(HospitalOnboardingRequest r) {
        return new HospitalOnboardingResponse(
                r.getId(), r.getHospitalName(), r.getCountryOrRegion(), r.getContactEmail(),
                r.getContactPhone(), r.getHospitalType(), r.getEstimatedBeds(), r.getPrimaryNeed(),
                r.getStatus(), r.getReviewNotes(), r.getReviewedByUserId(), r.getReviewedAt(),
                r.getProvisionedHospitalId(), r.getCreatedAt());
    }

    private String generateCode(String name) {
        String stripped = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return stripped.length() > 8 ? stripped.substring(0, 8) : stripped;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
