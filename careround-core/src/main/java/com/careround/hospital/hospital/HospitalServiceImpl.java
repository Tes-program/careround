package com.careround.hospital.hospital;

import com.careround.hospital.entity.Hospital;
import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.hospital.dto.CreateHospitalRequest;
import com.careround.hospital.hospital.dto.HospitalResponse;
import com.careround.hospital.repository.HospitalRepository;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;

    @Override
    @Transactional
    public HospitalResponse register(CreateHospitalRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.name());
        hospital.setAddress(request.address());
        hospital.setContactEmail(request.contactEmail());
        hospital.setContactPhone(request.contactPhone());
        hospital = hospitalRepository.save(hospital);

        SystemConfiguration config = new SystemConfiguration();
        config.setHospitalId(hospital.getId());
        systemConfigurationRepository.save(config);

        return toResponse(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalResponse getById(String hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        return toResponse(hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalResponse> listAll() {
        return hospitalRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private HospitalResponse toResponse(Hospital h) {
        return new HospitalResponse(h.getId(), h.getName(), h.getAddress(),
                h.getContactEmail(), h.getContactPhone(), h.getCreatedAt());
    }
}
