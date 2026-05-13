package com.careround.hospital.shift;

import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;
import com.careround.shared.event.ShiftActivatedEvent;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final WardRepository wardRepository;
    private final UserRepository userRepository;
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
        validateNurseInCharge(hospitalId, request.nurseInChargeId());

        shift.setLeadDoctorId(request.leadDoctorId());
        shift.setNurseInChargeId(request.nurseInChargeId());
        shift.setStatus(ShiftStatus.ACTIVE);
        shift.setAssignedAt(LocalDateTime.now(ZoneOffset.UTC));

        outboxService.publish("SHIFT_ACTIVATED",
                new ShiftActivatedEvent(
                        hospitalId,
                        shift.getId(),
                        shift.getWardId(),
                        shift.getLeadDoctorId(),
                        shift.getNurseInChargeId(),
                        shift.getStatus().name(),
                        shift.getAssignedAt(),
                        MDC.get("correlationId")
                ),
                hospitalId);
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

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> listShifts(String hospitalId, String wardId, ShiftStatus status, LocalDateTime from, LocalDateTime to) {
        wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new AccessDeniedException("Ward does not belong to this hospital"));

        LocalDateTime effectiveFrom = from == null ? LocalDateTime.now(ZoneOffset.UTC).minusDays(1) : from;
        LocalDateTime effectiveTo = to == null ? effectiveFrom.plusDays(14) : to;

        return shiftRepository.findAllByWardIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByStartTimeAsc(
                        wardId, effectiveTo, effectiveFrom)
                .stream()
                .filter(shift -> status == null || shift.getStatus() == status)
                .map(this::toResponse)
                .toList();
    }

    private void validateNurseInCharge(String hospitalId, String nurseInChargeId) {
        User nurse = userRepository.findByIdAndHospitalId(nurseInChargeId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Nurse in charge not found"));
        if (nurse.getRole() != UserRole.NURSE || !nurse.isActive()) {
            throw new BusinessRuleException("Nurse in charge must be an active nurse");
        }
    }

    private ShiftResponse toResponse(Shift s) {
        return new ShiftResponse(s.getId(), s.getWardId(), s.getShiftScheduleId(),
                s.getType(), s.getStartTime(), s.getEndTime(),
                s.getLeadDoctorId(), s.getNurseInChargeId(),
                s.getStatus(), s.getAssignedAt(), s.getCreatedAt());
    }
}
