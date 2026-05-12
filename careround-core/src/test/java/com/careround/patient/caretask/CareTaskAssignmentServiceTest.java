package com.careround.patient.caretask;

import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.enums.ShiftType;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareTaskAssignmentServiceTest {

    @Mock private ShiftRepository shiftRepository;
    @Mock private WardRepository wardRepository;
    @Mock private CareTaskRepository careTaskRepository;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String WARD_ID = "ward-1";
    private static final LocalDateTime START = LocalDateTime.of(2026, 5, 12, 9, 0);
    private static final LocalDateTime END = START.plusMinutes(30);

    @Test
    void assignForNewTask_wardNurseAvailable_assignsWardNurse() {
        CareTaskAssignmentService service = service();
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(ward(WARD_ID, "Cardiology")));
        when(shiftRepository.findFirstByWardIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeDesc(
                WARD_ID, ShiftStatus.ACTIVE, END, START))
                .thenReturn(Optional.of(shift(WARD_ID, "nurse-1")));
        when(careTaskRepository.existsByHospitalIdAndAssignedToIdAndStatusInAndWindowStartLessThanAndWindowEndGreaterThan(
                HOSPITAL_ID, "nurse-1", List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS), END, START))
                .thenReturn(false);

        CareTaskAssignmentResult result = service.assignForNewTask(HOSPITAL_ID, WARD_ID, START, END);

        assertThat(result.nurseId()).isEqualTo("nurse-1");
        assertThat(result.workloadConflict()).isFalse();
    }

    @Test
    void assignForNewTask_wardNurseBusy_assignsSameSpecialtyAvailableNurse() {
        CareTaskAssignmentService service = service();
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(ward(WARD_ID, "Cardiology")));
        when(shiftRepository.findFirstByWardIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeDesc(
                WARD_ID, ShiftStatus.ACTIVE, END, START))
                .thenReturn(Optional.of(shift(WARD_ID, "nurse-1")));
        when(careTaskRepository.existsByHospitalIdAndAssignedToIdAndStatusInAndWindowStartLessThanAndWindowEndGreaterThan(
                HOSPITAL_ID, "nurse-1", List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS), END, START))
                .thenReturn(true);
        when(wardRepository.findAllByHospitalId(HOSPITAL_ID)).thenReturn(List.of(
                ward(WARD_ID, "Cardiology"),
                ward("ward-2", "Cardiology"),
                ward("ward-3", "Neurology")));
        when(shiftRepository.findAllByStatusAndStartTimeLessThanAndEndTimeGreaterThan(ShiftStatus.ACTIVE, END, START))
                .thenReturn(List.of(
                        shift(WARD_ID, "nurse-1"),
                        shift("ward-2", "nurse-2"),
                        shift("ward-3", "nurse-3")));
        when(careTaskRepository.existsByHospitalIdAndAssignedToIdAndStatusInAndWindowStartLessThanAndWindowEndGreaterThan(
                HOSPITAL_ID, "nurse-2", List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS), END, START))
                .thenReturn(false);

        CareTaskAssignmentResult result = service.assignForNewTask(HOSPITAL_ID, WARD_ID, START, END);

        assertThat(result.nurseId()).isEqualTo("nurse-2");
        assertThat(result.workloadConflict()).isFalse();
    }

    @Test
    void assignForNewTask_allSameSpecialtyNursesBusy_assignsWardNurseWithConflict() {
        CareTaskAssignmentService service = service();
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(ward(WARD_ID, "Cardiology")));
        when(shiftRepository.findFirstByWardIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeDesc(
                WARD_ID, ShiftStatus.ACTIVE, END, START))
                .thenReturn(Optional.of(shift(WARD_ID, "nurse-1")));
        when(careTaskRepository.existsByHospitalIdAndAssignedToIdAndStatusInAndWindowStartLessThanAndWindowEndGreaterThan(
                HOSPITAL_ID, "nurse-1", List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS), END, START))
                .thenReturn(true);
        when(wardRepository.findAllByHospitalId(HOSPITAL_ID)).thenReturn(List.of(
                ward(WARD_ID, "Cardiology"),
                ward("ward-2", "Cardiology")));
        when(shiftRepository.findAllByStatusAndStartTimeLessThanAndEndTimeGreaterThan(ShiftStatus.ACTIVE, END, START))
                .thenReturn(List.of(shift(WARD_ID, "nurse-1"), shift("ward-2", "nurse-2")));
        when(careTaskRepository.existsByHospitalIdAndAssignedToIdAndStatusInAndWindowStartLessThanAndWindowEndGreaterThan(
                HOSPITAL_ID, "nurse-2", List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS), END, START))
                .thenReturn(true);

        CareTaskAssignmentResult result = service.assignForNewTask(HOSPITAL_ID, WARD_ID, START, END);

        assertThat(result.nurseId()).isEqualTo("nurse-1");
        assertThat(result.workloadConflict()).isTrue();
        assertThat(result.workloadConflictReason()).contains("overlapping care tasks");
    }

    private CareTaskAssignmentService service() {
        return new CareTaskAssignmentService(shiftRepository, wardRepository, careTaskRepository);
    }

    private Ward ward(String id, String specialty) {
        Ward ward = new Ward();
        ward.setId(id);
        ward.setHospitalId(HOSPITAL_ID);
        ward.setSpecialty(specialty);
        return ward;
    }

    private Shift shift(String wardId, String nurseId) {
        Shift shift = new Shift();
        shift.setWardId(wardId);
        shift.setNurseInChargeId(nurseId);
        shift.setStatus(ShiftStatus.ACTIVE);
        shift.setType(ShiftType.DAY);
        shift.setStartTime(START.minusHours(1));
        shift.setEndTime(END.plusHours(1));
        return shift;
    }
}
