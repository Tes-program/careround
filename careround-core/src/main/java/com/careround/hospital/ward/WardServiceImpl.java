package com.careround.hospital.ward;

import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.ward.dto.CreateWardRequest;
import com.careround.hospital.ward.dto.UpdateWardRequest;
import com.careround.hospital.ward.dto.WardResponse;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WardServiceImpl implements WardService {

    private final WardRepository wardRepository;

    @Override
    @Transactional
    public WardResponse create(String hospitalId, CreateWardRequest request) {
        Ward ward = new Ward();
        ward.setHospitalId(hospitalId);
        ward.setName(request.name());
        ward.setSpecialty(request.specialty());
        ward.setTotalBeds(request.totalBeds());
        ward.setSupervisorId(request.supervisorId());
        return toResponse(wardRepository.save(ward));
    }

    @Override
    @Transactional(readOnly = true)
    public WardResponse getById(String hospitalId, String wardId) {
        return wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WardResponse> listByHospital(String hospitalId) {
        return wardRepository.findAllByHospitalId(hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public WardResponse update(String hospitalId, String wardId, UpdateWardRequest request) {
        Ward ward = wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        if (request.name() != null) ward.setName(request.name());
        if (request.specialty() != null) ward.setSpecialty(request.specialty());
        if (request.totalBeds() != null) ward.setTotalBeds(request.totalBeds());
        if (request.supervisorId() != null) ward.setSupervisorId(request.supervisorId());
        return toResponse(ward);
    }

    @Override
    @Transactional
    public void delete(String hospitalId, String wardId) {
        Ward ward = wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        wardRepository.delete(ward);
    }

    private WardResponse toResponse(Ward w) {
        return new WardResponse(w.getId(), w.getHospitalId(), w.getName(),
                w.getSpecialty(), w.getTotalBeds(), w.getSupervisorId(), w.getCreatedAt());
    }
}
