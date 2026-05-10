package com.careround.hospital.oncall;

import com.careround.hospital.entity.OnCallRotation;
import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.oncall.dto.CreateOnCallRotationRequest;
import com.careround.hospital.oncall.dto.OnCallRotationResponse;
import com.careround.hospital.repository.OnCallRotationRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnCallRotationServiceTest {

    @Mock private OnCallRotationRepository repository;
    @InjectMocks private OnCallRotationServiceImpl service;

    private OnCallRotation rotation;
    private static final LocalDateTime START = LocalDateTime.now().minusHours(2);
    private static final LocalDateTime END = LocalDateTime.now().plusHours(6);

    @BeforeEach
    void setUp() {
        rotation = new OnCallRotation();
        rotation.setId("rot-1");
        rotation.setHospitalId("hosp-1");
        rotation.setDepartmentId("dept-1");
        rotation.setDoctorId("doctor-1");
        rotation.setRole(OnCallRole.CONSULTANT_ON_CALL);
        rotation.setStartTime(START);
        rotation.setEndTime(END);
    }

    @Test
    void create_happyPath_shouldSaveAndReturn() {
        when(repository.save(any())).thenAnswer(i -> {
            OnCallRotation r = i.getArgument(0);
            r.setId("rot-new");
            return r;
        });

        OnCallRotationResponse result = service.create("hosp-1",
                new CreateOnCallRotationRequest("dept-1", null, "doctor-1",
                        OnCallRole.CONSULTANT_ON_CALL, START, END));

        assertThat(result.id()).isEqualTo("rot-new");
        assertThat(result.role()).isEqualTo(OnCallRole.CONSULTANT_ON_CALL);
    }

    @Test
    void getById_withUnknownId_shouldThrowResourceNotFoundException() {
        when(repository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("hosp-1", "bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_crossTenantAccess_shouldThrowResourceNotFoundException() {
        when(repository.findByIdAndHospitalId("rot-1", "other-hosp")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("other-hosp", "rot-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByHospital_shouldReturnAllRotations() {
        when(repository.findAllByHospitalId("hosp-1")).thenReturn(List.of(rotation));

        List<OnCallRotationResponse> results = service.listByHospital("hosp-1");

        assertThat(results).hasSize(1);
    }

    @Test
    void getCurrentOnCall_whenActiveRotationExists_shouldReturnIt() {
        when(repository.findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                any(), any(), any(), any(), any())).thenReturn(Optional.of(rotation));

        Optional<OnCallRotationResponse> result = service.getCurrentOnCall(
                "hosp-1", "dept-1", OnCallRole.CONSULTANT_ON_CALL);

        assertThat(result).isPresent();
        assertThat(result.get().doctorId()).isEqualTo("doctor-1");
    }

    @Test
    void getCurrentOnCall_whenNoActiveRotation_shouldReturnEmpty() {
        when(repository.findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                any(), any(), any(), any(), any())).thenReturn(Optional.empty());

        Optional<OnCallRotationResponse> result = service.getCurrentOnCall(
                "hosp-1", "dept-1", OnCallRole.CONSULTANT_ON_CALL);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_withValidIds_shouldDelete() {
        when(repository.findByIdAndHospitalId("rot-1", "hosp-1")).thenReturn(Optional.of(rotation));

        service.delete("hosp-1", "rot-1");

        verify(repository).delete(rotation);
    }
}
