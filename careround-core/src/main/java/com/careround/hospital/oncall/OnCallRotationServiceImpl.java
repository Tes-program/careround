package com.careround.hospital.oncall;

import com.careround.hospital.entity.OnCallRotation;
import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.oncall.dto.CreateOnCallRotationRequest;
import com.careround.hospital.oncall.dto.OnCallRotationResponse;
import com.careround.hospital.repository.OnCallRotationRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OnCallRotationServiceImpl implements OnCallRotationService {

    private final OnCallRotationRepository onCallRotationRepository;

    @Override
    @Transactional
    public OnCallRotationResponse create(String hospitalId, CreateOnCallRotationRequest request) {
        OnCallRotation rotation = new OnCallRotation();
        rotation.setHospitalId(hospitalId);
        rotation.setDepartmentId(request.departmentId());
        rotation.setWardId(request.wardId());
        rotation.setDoctorId(request.doctorId());
        rotation.setRole(request.role());
        rotation.setStartTime(request.startTime());
        rotation.setEndTime(request.endTime());
        return toResponse(onCallRotationRepository.save(rotation));
    }

    @Override
    @Transactional(readOnly = true)
    public OnCallRotationResponse getById(String hospitalId, String rotationId) {
        return onCallRotationRepository.findByIdAndHospitalId(rotationId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("On-call rotation not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnCallRotationResponse> listByHospital(String hospitalId) {
        return onCallRotationRepository.findAllByHospitalId(hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OnCallRotationResponse> getCurrentOnCall(String hospitalId, String departmentId, OnCallRole role) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return onCallRotationRepository
                .findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                        hospitalId, departmentId, role, now, now)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(String hospitalId, String rotationId) {
        OnCallRotation rotation = onCallRotationRepository.findByIdAndHospitalId(rotationId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("On-call rotation not found"));
        onCallRotationRepository.delete(rotation);
    }

    private OnCallRotationResponse toResponse(OnCallRotation r) {
        return new OnCallRotationResponse(r.getId(), r.getHospitalId(), r.getDepartmentId(),
                r.getWardId(), r.getDoctorId(), r.getRole(), r.getStartTime(),
                r.getEndTime(), r.getCreatedAt());
    }
}
