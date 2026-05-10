package com.careround.hospital.shift;

import com.careround.hospital.entity.Shift;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final WardRepository wardRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public ShiftResponse assignStaff(String hospitalId, String shiftId, AssignStaffRequest request) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));

        wardRepository.findByIdAndHospitalId(shift.getWardId(), hospitalId)
                .orElseThrow(() -> new AccessDeniedException("Shift does not belong to this hospital"));

        if (shift.getStatus() != ShiftStatus.PENDING_ASSIGNMENT) {
            throw new BusinessRuleException(
                    "Shift cannot be assigned; current status is " + shift.getStatus());
        }

        shift.setLeadDoctorId(request.leadDoctorId());
        shift.setNurseInChargeId(request.nurseInChargeId());
        shift.setStatus(ShiftStatus.ACTIVE);
        shift.setAssignedAt(LocalDateTime.now(ZoneOffset.UTC));

        outboxService.publish("SHIFT_ACTIVATED", shift, hospitalId);
        return toResponse(shift);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getCurrentShift(String hospitalId, String wardId) {
        wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new AccessDeniedException("Ward does not belong to this hospital"));

        return shiftRepository.findFirstByWardIdAndStatusOrderByStartTimeDesc(wardId, ShiftStatus.ACTIVE)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No active shift found for ward"));
    }

    private ShiftResponse toResponse(Shift s) {
        return new ShiftResponse(s.getId(), s.getWardId(), s.getShiftScheduleId(),
                s.getType(), s.getStartTime(), s.getEndTime(),
                s.getLeadDoctorId(), s.getNurseInChargeId(),
                s.getStatus(), s.getAssignedAt(), s.getCreatedAt());
    }
}
