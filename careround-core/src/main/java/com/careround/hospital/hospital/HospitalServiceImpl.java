package com.careround.hospital.hospital;

import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Hospital;
import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.hospital.dto.HospitalRegistrationResponse;
import com.careround.hospital.hospital.dto.HospitalResponse;
import com.careround.hospital.hospital.dto.RegisterHospitalRequest;
import com.careround.hospital.hospital.dto.UpdateHospitalRequest;
import com.careround.hospital.repository.HospitalRepository;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.shared.exception.ConflictException;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public HospitalRegistrationResponse register(RegisterHospitalRequest request) {
        String code = generateCode(request.code(), request.name());

        if (hospitalRepository.existsByCode(code)) {
            throw new ConflictException("Hospital code already exists");
        }
        if (hospitalRepository.existsByContactEmail(request.contactEmail())) {
            throw new ConflictException("Hospital contact email already exists");
        }

        Hospital hospital = new Hospital();
        hospital.setName(request.name());
        hospital.setCode(code);
        hospital.setAddress(request.address());
        hospital.setContactEmail(request.contactEmail());
        hospital.setContactPhone(request.contactPhone());
        hospital = hospitalRepository.save(hospital);

        SystemConfiguration config = new SystemConfiguration();
        config.setHospitalId(hospital.getId());
        systemConfigurationRepository.save(config);

        User admin = new User();
        admin.setHospitalId(hospital.getId());
        admin.setFirstName(request.adminFirstName());
        admin.setLastName(request.adminLastName());
        admin.setEmail(request.adminEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin = userRepository.save(admin);

        return new HospitalRegistrationResponse(hospital.getId(), code, admin.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalResponse getById(String hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return toResponse(hospital);
    }

    @Override
    @Transactional
    public HospitalResponse update(String hospitalId, UpdateHospitalRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        if (!hospital.getContactEmail().equalsIgnoreCase(request.contactEmail())
                && hospitalRepository.existsByContactEmail(request.contactEmail())) {
            throw new ConflictException("Hospital contact email already exists");
        }
        if (!hospital.getCode().equalsIgnoreCase(request.code())
                && hospitalRepository.existsByCode(request.code())) {
            throw new ConflictException("Hospital code already exists");
        }

        hospital.setName(request.name());
        hospital.setCode(request.code());
        hospital.setAddress(request.address());
        hospital.setContactEmail(request.contactEmail());
        hospital.setContactPhone(request.contactPhone());
        return toResponse(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalResponse> listAll() {
        return hospitalRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    String generateCode(String providedCode, String name) {
        if (providedCode != null && !providedCode.isBlank()) {
            return providedCode.toUpperCase();
        }
        String stripped = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return stripped.length() > 8 ? stripped.substring(0, 8) : stripped;
    }

    private HospitalResponse toResponse(Hospital h) {
        return new HospitalResponse(h.getId(), h.getName(), h.getCode(), h.getAddress(),
                h.getContactEmail(), h.getContactPhone(), h.isActive(), h.getCreatedAt());
    }
}
