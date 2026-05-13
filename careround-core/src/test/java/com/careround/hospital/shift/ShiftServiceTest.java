package com.careround.hospital.shift;

import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.enums.ShiftType;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock private ShiftRepository shiftRepository;
    @Mock private WardRepository wardRepository;
    @Mock private UserRepository userRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private ShiftServiceImpl shiftService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String WARD_ID = "ward-1";
    private static final String SHIFT_ID = "shift-1";

    private Shift pendingShift;
    private Shift activeShift;
    private Ward ward;

    @BeforeEach
    void setUp() {
        ward = new Ward();
        ward.setId(WARD_ID);
        ward.setHospitalId(HOSPITAL_ID);

        pendingShift = new Shift();
        pendingShift.setId(SHIFT_ID);
        pendingShift.setWardId(WARD_ID);
        pendingShift.setType(ShiftType.DAY);
        pendingShift.setStartTime(LocalDateTime.now());
        pendingShift.setEndTime(LocalDateTime.now().plusHours(12));
        pendingShift.setStatus(ShiftStatus.PENDING_ASSIGNMENT);

        activeShift = new Shift();
        activeShift.setId("shift-active");
        activeShift.setWardId(WARD_ID);
        activeShift.setType(ShiftType.DAY);
        activeShift.setStartTime(LocalDateTime.now());
        activeShift.setEndTime(LocalDateTime.now().plusHours(12));
        activeShift.setStatus(ShiftStatus.ACTIVE);
        activeShift.setLeadDoctorId("doctor-1");
        activeShift.setNurseInChargeId("nurse-1");
    }

    @Test
    void assignStaff_happyPath_shouldTransitionToActiveAndPublishEvent() {
        when(shiftRepository.findById(SHIFT_ID)).thenReturn(Optional.of(pendingShift));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(userRepository.findByIdAndHospitalId("nurse-1", HOSPITAL_ID)).thenReturn(Optional.of(user("nurse-1", UserRole.NURSE, true)));

        ShiftResponse result = shiftService.assignStaff(HOSPITAL_ID, SHIFT_ID,
                new AssignStaffRequest("doctor-1", "nurse-1"));

        assertThat(result.status()).isEqualTo(ShiftStatus.ACTIVE);
        assertThat(result.leadDoctorId()).isEqualTo("doctor-1");
        assertThat(result.nurseInChargeId()).isEqualTo("nurse-1");
        assertThat(result.assignedAt()).isNotNull();
        verify(outboxService).publish(eq("SHIFT_ACTIVATED"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void assignStaff_whenNurseInChargeIsNotNurse_shouldThrowBusinessRuleException() {
        when(shiftRepository.findById(SHIFT_ID)).thenReturn(Optional.of(pendingShift));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(userRepository.findByIdAndHospitalId("consultant-1", HOSPITAL_ID))
                .thenReturn(Optional.of(user("consultant-1", UserRole.CONSULTANT, true)));

        assertThatThrownBy(() -> shiftService.assignStaff(HOSPITAL_ID, SHIFT_ID,
                new AssignStaffRequest("doctor-1", "consultant-1")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("active nurse");
    }

    @Test
    void assignStaff_whenNurseInChargeIsInactive_shouldThrowBusinessRuleException() {
        when(shiftRepository.findById(SHIFT_ID)).thenReturn(Optional.of(pendingShift));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(userRepository.findByIdAndHospitalId("nurse-1", HOSPITAL_ID))
                .thenReturn(Optional.of(user("nurse-1", UserRole.NURSE, false)));

        assertThatThrownBy(() -> shiftService.assignStaff(HOSPITAL_ID, SHIFT_ID,
                new AssignStaffRequest("doctor-1", "nurse-1")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("active nurse");
    }

    @Test
    void assignStaff_allowsNurseInChargeOnMultipleOverlappingWardShifts() {
        when(shiftRepository.findById(SHIFT_ID)).thenReturn(Optional.of(pendingShift));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(userRepository.findByIdAndHospitalId("nurse-1", HOSPITAL_ID)).thenReturn(Optional.of(user("nurse-1", UserRole.NURSE, true)));

        ShiftResponse result = shiftService.assignStaff(HOSPITAL_ID, SHIFT_ID,
                new AssignStaffRequest("doctor-1", "nurse-1"));

        assertThat(result.status()).isEqualTo(ShiftStatus.ACTIVE);
        assertThat(result.nurseInChargeId()).isEqualTo("nurse-1");
    }

    @Test
    void assignStaff_onAlreadyActiveShift_shouldThrowBusinessRuleException() {
        when(shiftRepository.findById("shift-active")).thenReturn(Optional.of(activeShift));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));

        assertThatThrownBy(() -> shiftService.assignStaff(HOSPITAL_ID, "shift-active",
                new AssignStaffRequest("doctor-1", "nurse-1")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void assignStaff_withUnknownShift_shouldThrowResourceNotFoundException() {
        when(shiftRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shiftService.assignStaff(HOSPITAL_ID, "bad",
                new AssignStaffRequest("doctor-1", "nurse-1")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignStaff_crossTenantAccess_shouldThrowAccessDeniedException() {
        when(shiftRepository.findById(SHIFT_ID)).thenReturn(Optional.of(pendingShift));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, "other-hosp")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shiftService.assignStaff("other-hosp", SHIFT_ID,
                new AssignStaffRequest("doctor-1", "nurse-1")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getCurrentShift_whenActiveShiftExists_shouldReturnIt() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(shiftRepository.findFirstByWardIdAndStatusOrderByStartTimeDesc(WARD_ID, ShiftStatus.ACTIVE))
                .thenReturn(Optional.of(activeShift));

        ShiftResponse result = shiftService.getCurrentShift(HOSPITAL_ID, WARD_ID);

        assertThat(result.status()).isEqualTo(ShiftStatus.ACTIVE);
        assertThat(result.leadDoctorId()).isEqualTo("doctor-1");
    }

    @Test
    void getCurrentShift_whenNoActiveShift_shouldThrowResourceNotFoundException() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(shiftRepository.findFirstByWardIdAndStatusOrderByStartTimeDesc(WARD_ID, ShiftStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shiftService.getCurrentShift(HOSPITAL_ID, WARD_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No active shift");
    }

    @Test
    void getCurrentShift_crossTenantAccess_shouldThrowAccessDeniedException() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, "other-hosp")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shiftService.getCurrentShift("other-hosp", WARD_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    private User user(String id, UserRole role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setHospitalId(HOSPITAL_ID);
        user.setRole(role);
        user.setActive(active);
        return user;
    }
}
