package com.careround.onboarding.service;

import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.auth.service.AccountActivationService;
import com.careround.hospital.entity.Hospital;
import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.repository.HospitalRepository;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.onboarding.dto.CreateHospitalOnboardingRequest;
import com.careround.onboarding.dto.HospitalOnboardingResponse;
import com.careround.onboarding.dto.ProvisionHospitalTenantRequest;
import com.careround.onboarding.dto.ProvisionHospitalTenantResponse;
import com.careround.onboarding.dto.ReviewHospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import com.careround.onboarding.repository.HospitalOnboardingRequestRepository;
import com.careround.shared.event.HospitalOnboardingRequestedEvent;
import com.careround.shared.event.HospitalOnboardingReviewedEvent;
import com.careround.shared.event.HospitalProvisionedEvent;
import com.careround.shared.event.UserActivationRequestedEvent;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ConflictException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HospitalOnboardingService {

    private static final String PLATFORM_OUTBOX_SCOPE = "platform";
    private static final List<HospitalOnboardingStatus> ACTIVE_REQUEST_STATUSES = List.of(
            HospitalOnboardingStatus.PENDING_REVIEW,
            HospitalOnboardingStatus.CONTACTED,
            HospitalOnboardingStatus.APPROVED
    );

    private final HospitalOnboardingRequestRepository onboardingRequestRepository;
    private final HospitalRepository hospitalRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final UserRepository userRepository;
    private final AccountActivationService accountActivationService;
    private final PasswordEncoder passwordEncoder;
    private final OutboxService outboxService;

    @Value("${careround.app.activation-base-url:http://localhost:3000/activate}")
    private String activationBaseUrl;

    @Transactional
    public HospitalOnboardingResponse submit(CreateHospitalOnboardingRequest request) {
        String email = normalizeEmail(request.contactEmail());
        if (onboardingRequestRepository.existsByContactEmailAndStatusIn(email, ACTIVE_REQUEST_STATUSES)) {
            throw new ConflictException("An onboarding request for this contact email is already pending.");
        }
        if (hospitalRepository.existsByContactEmail(email)) {
            throw new ConflictException("A hospital with this contact email already exists.");
        }

        HospitalOnboardingRequest entity = new HospitalOnboardingRequest();
        entity.setHospitalName(request.hospitalName());
        entity.setCountryOrRegion(request.countryOrRegion());
        entity.setContactEmail(email);
        entity.setContactPhone(request.contactPhone());
        entity.setHospitalType(request.hospitalType());
        entity.setEstimatedInpatientBeds(request.estimatedInpatientBeds());
        entity.setPrimaryNeed(request.primaryNeed());
        entity.setStatus(HospitalOnboardingStatus.PENDING_REVIEW);
        HospitalOnboardingRequest saved = onboardingRequestRepository.save(entity);

        outboxService.publish("careround.hospital.onboarding_requested",
                new HospitalOnboardingRequestedEvent(PLATFORM_OUTBOX_SCOPE, saved.getId(), saved.getHospitalName(),
                        saved.getContactEmail(), MDC.get("correlationId")),
                PLATFORM_OUTBOX_SCOPE);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HospitalOnboardingResponse> list(HospitalOnboardingStatus status, int limit, String cursor) {
        int effectiveLimit = limit <= 0 ? 20 : Math.min(limit, 100);
        PageRequest pageRequest = PageRequest.of(0, effectiveLimit);
        List<HospitalOnboardingRequest> requests;
        if (cursor != null && !cursor.isBlank()) {
            LocalDateTime cursorCreatedAt = onboardingRequestRepository.findById(cursor)
                    .orElseThrow(() -> new ResourceNotFoundException("Cursor onboarding request not found"))
                    .getCreatedAt();
            requests = status == null
                    ? onboardingRequestRepository.findAllByCreatedAtBeforeOrderByCreatedAtDesc(cursorCreatedAt, pageRequest)
                    : onboardingRequestRepository.findAllByStatusAndCreatedAtBeforeOrderByCreatedAtDesc(
                            status, cursorCreatedAt, pageRequest);
        } else {
            requests = status == null
                    ? onboardingRequestRepository.findAllByOrderByCreatedAtDesc(pageRequest)
                    : onboardingRequestRepository.findAllByStatusOrderByCreatedAtDesc(status, pageRequest);
        }
        return requests.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HospitalOnboardingResponse get(String id) {
        return onboardingRequestRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));
    }

    @Transactional
    public HospitalOnboardingResponse review(String id, ReviewHospitalOnboardingRequest request) {
        HospitalOnboardingRequest entity = onboardingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));
        if (entity.getStatus() == HospitalOnboardingStatus.PROVISIONED) {
            throw new ConflictException("Provisioned onboarding requests cannot be reviewed.");
        }
        if (request.status() != HospitalOnboardingStatus.CONTACTED
                && request.status() != HospitalOnboardingStatus.APPROVED
                && request.status() != HospitalOnboardingStatus.REJECTED) {
            throw new BusinessRuleException("Review status must be CONTACTED, APPROVED, or REJECTED");
        }

        entity.setStatus(request.status());
        entity.setReviewNotes(request.reviewNotes());
        entity.setReviewedByUserId(currentOperatorId());
        entity.setReviewedAt(LocalDateTime.now(ZoneOffset.UTC));

        outboxService.publish("careround.hospital.onboarding_reviewed",
                new HospitalOnboardingReviewedEvent(PLATFORM_OUTBOX_SCOPE, entity.getId(), entity.getStatus().name(),
                        entity.getReviewedByUserId(), MDC.get("correlationId")),
                PLATFORM_OUTBOX_SCOPE);

        return toResponse(entity);
    }

    @Transactional
    public ProvisionHospitalTenantResponse provision(String id, ProvisionHospitalTenantRequest request) {
        HospitalOnboardingRequest onboardingRequest = onboardingRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));
        if (onboardingRequest.getStatus() == HospitalOnboardingStatus.PROVISIONED) {
            throw new ConflictException("This onboarding request has already been provisioned.");
        }
        if (onboardingRequest.getStatus() != HospitalOnboardingStatus.APPROVED) {
            throw new BusinessRuleException("Only approved onboarding requests can be provisioned.");
        }

        String hospitalEmail = normalizeEmail(request.contactEmail());
        String adminEmail = normalizeEmail(request.adminEmail());
        if (hospitalRepository.existsByContactEmail(hospitalEmail)) {
            throw new ConflictException("A hospital with this contact email already exists.");
        }

        Hospital hospital = new Hospital();
        hospital.setName(request.hospitalName());
        hospital.setAddress(request.address());
        hospital.setContactEmail(hospitalEmail);
        hospital.setContactPhone(request.contactPhone());
        hospital = hospitalRepository.save(hospital);

        SystemConfiguration configuration = new SystemConfiguration();
        configuration.setHospitalId(hospital.getId());
        configuration.setNewsAmberThreshold(request.newsAmberThreshold());
        configuration.setNewsRedThreshold(request.newsRedThreshold());
        configuration.setTaskOverdueGraceMinutes(request.taskOverdueGraceMinutes());
        configuration.setRoundNotificationsEnabled(request.roundNotificationsEnabled());
        configuration.setNokNotificationEnabled(request.nokNotificationEnabled());
        systemConfigurationRepository.save(configuration);

        if (userRepository.existsByHospitalIdAndEmail(hospital.getId(), adminEmail)) {
            throw new ConflictException("A user with this admin email already exists in the hospital.");
        }
        User admin = new User();
        admin.setHospitalId(hospital.getId());
        admin.setFirstName(request.adminFirstName());
        admin.setLastName(request.adminLastName());
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(false);
        admin = userRepository.save(admin);

        String activationToken = accountActivationService.createToken(admin, 72);
        String activationUrl = activationBaseUrl + "?token=" + activationToken;

        onboardingRequest.setStatus(HospitalOnboardingStatus.PROVISIONED);
        onboardingRequest.setProvisionedHospitalId(hospital.getId());
        onboardingRequest.setReviewedByUserId(currentOperatorId());
        onboardingRequest.setReviewedAt(LocalDateTime.now(ZoneOffset.UTC));

        outboxService.publish("careround.hospital.provisioned",
                new HospitalProvisionedEvent(onboardingRequest.getId(), hospital.getId(),
                        admin.getId(), MDC.get("correlationId")),
                hospital.getId());
        outboxService.publish("careround.user.activation_requested",
                new UserActivationRequestedEvent(hospital.getId(), admin.getId(), admin.getEmail(),
                        activationUrl, MDC.get("correlationId")),
                hospital.getId());

        return new ProvisionHospitalTenantResponse(onboardingRequest.getId(), hospital.getId(),
                admin.getId(), onboardingRequest.getStatus());
    }

    private HospitalOnboardingResponse toResponse(HospitalOnboardingRequest request) {
        return new HospitalOnboardingResponse(
                request.getId(),
                request.getHospitalName(),
                request.getCountryOrRegion(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getHospitalType(),
                request.getEstimatedInpatientBeds(),
                request.getPrimaryNeed(),
                request.getStatus(),
                request.getReviewNotes(),
                request.getReviewedByUserId(),
                request.getReviewedAt(),
                request.getProvisionedHospitalId(),
                request.getCreatedAt(),
                request.getUpdatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String currentOperatorId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
