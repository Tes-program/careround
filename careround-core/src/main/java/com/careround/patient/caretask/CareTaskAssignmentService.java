package com.careround.patient.caretask;

import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CareTaskAssignmentService {

    private static final List<TaskStatus> ACTIVE_ASSIGNMENT_STATUSES = List.of(
            TaskStatus.PENDING,
            TaskStatus.IN_PROGRESS
    );

    private final ShiftRepository shiftRepository;
    private final WardRepository wardRepository;
    private final CareTaskRepository careTaskRepository;

    public CareTaskAssignmentResult assignForNewTask(
            String hospitalId,
            String patientWardId,
            LocalDateTime windowStart,
            LocalDateTime windowEnd) {
        Ward patientWard = wardRepository.findByIdAndHospitalId(patientWardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        Shift wardShift = shiftRepository
                .findFirstByWardIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeDesc(
                        patientWardId, ShiftStatus.ACTIVE, windowEnd, windowStart)
                .orElseThrow(() -> new BusinessRuleException("No active shift found for the patient's ward at this task time"));

        if (!StringUtils.hasText(wardShift.getNurseInChargeId())) {
            throw new BusinessRuleException("The active ward shift has no nurse in charge");
        }

        String wardNurseId = wardShift.getNurseInChargeId();
        if (!hasConflict(hospitalId, wardNurseId, windowStart, windowEnd)) {
            return new CareTaskAssignmentResult(wardNurseId, false, null);
        }

        return fallbackNurse(hospitalId, patientWard, wardNurseId, windowStart, windowEnd)
                .stream()
                .filter(nurseId -> !hasConflict(hospitalId, nurseId, windowStart, windowEnd))
                .findFirst()
                .map(nurseId -> new CareTaskAssignmentResult(nurseId, false, null))
                .orElseGet(() -> new CareTaskAssignmentResult(
                        wardNurseId,
                        true,
                        "All active same-specialty nurses have overlapping care tasks; assigned to ward nurse in charge."));
    }

    public boolean hasConflictExcludingTask(
            String hospitalId,
            String nurseId,
            String taskId,
            LocalDateTime windowStart,
            LocalDateTime windowEnd) {
        return careTaskRepository.existsOverlappingAssignedTaskExcludingTask(
                hospitalId,
                nurseId,
                taskId,
                ACTIVE_ASSIGNMENT_STATUSES,
                windowStart,
                windowEnd);
    }

    private List<String> fallbackNurse(
            String hospitalId,
            Ward patientWard,
            String wardNurseId,
            LocalDateTime windowStart,
            LocalDateTime windowEnd) {
        List<Ward> sameSpecialtyWards = wardRepository.findAllByHospitalId(hospitalId).stream()
                .filter(ward -> isSameSpecialty(patientWard, ward))
                .toList();
        var sameSpecialtyWardIds = sameSpecialtyWards.stream()
                .map(Ward::getId)
                .collect(HashSet<String>::new, HashSet::add, HashSet::addAll);

        return shiftRepository.findAllByStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        ShiftStatus.ACTIVE, windowEnd, windowStart)
                .stream()
                .filter(shift -> sameSpecialtyWardIds.contains(shift.getWardId()))
                .map(Shift::getNurseInChargeId)
                .filter(StringUtils::hasText)
                .filter(nurseId -> !Objects.equals(nurseId, wardNurseId))
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private boolean hasConflict(String hospitalId, String nurseId, LocalDateTime windowStart, LocalDateTime windowEnd) {
        return careTaskRepository.existsByHospitalIdAndAssignedToIdAndStatusInAndWindowStartLessThanAndWindowEndGreaterThan(
                hospitalId,
                nurseId,
                ACTIVE_ASSIGNMENT_STATUSES,
                windowEnd,
                windowStart);
    }

    private boolean isSameSpecialty(Ward patientWard, Ward candidateWard) {
        if (!StringUtils.hasText(patientWard.getSpecialty())) {
            return Objects.equals(patientWard.getId(), candidateWard.getId());
        }
        return patientWard.getSpecialty().equalsIgnoreCase(candidateWard.getSpecialty());
    }
}
