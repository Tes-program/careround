package com.careround.hospital.supervisor;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.supervisor.dto.SupervisorDashboardResponse;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.AdmissionType;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupervisorDashboardServiceTest {

    @Mock private WardRepository wardRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private MedicationTaskRepository medicationTaskRepository;

    @InjectMocks private SupervisorDashboardServiceImpl service;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String WARD_ID = "ward-1";
    private static final String USER_ID = "user-supervisor";

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, USER_ID, UserRole.SUPERVISOR);
        lenient().when(medicationTaskRepository.countByWardIdAndHospitalIdAndStatus(any(), any(), any())).thenReturn(0L);
        lenient().when(medicationTaskRepository.countByWardIdAndHospitalIdAndStatusAndCompletedAtBetween(
                any(), any(), eq(MedicationTaskStatus.COMPLETED), any(), any())).thenReturn(0L);
        lenient().when(medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(medicationTaskRepository
                .findAllByWardIdAndHospitalIdAndStatusAndScheduledTimeBetweenOrderByScheduledTimeAsc(
                        any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        lenient().when(medicationTaskRepository
                .findAllByWardIdAndHospitalIdAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                        any(), any(), eq(MedicationTaskStatus.COMPLETED), any(), any()))
                .thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void getDashboard_returnsResponse_withPatientsSortedByAcuity() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward()));
        Patient redPatient = patient("p-1", AcuityColor.RED);
        Patient greenPatient = patient("p-2", AcuityColor.GREEN);
        when(patientRepository.findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(HOSPITAL_ID, WARD_ID))
                .thenReturn(List.of(redPatient, greenPatient));

        SupervisorDashboardResponse result = service.getDashboard(WARD_ID);

        assertThat(result.patients()).hasSize(2);
        assertThat(result.patients().getFirst().patientId()).isEqualTo("p-1");
    }

    @Test
    void getDashboard_throwsResourceNotFound_whenWardNotInHospital() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDashboard(WARD_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDashboard_returnsEmptyPatientList_whenNoPatients() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward()));
        when(patientRepository.findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(any(), any()))
                .thenReturn(List.of());

        SupervisorDashboardResponse result = service.getDashboard(WARD_ID);

        assertThat(result.patients()).isEmpty();
    }

    @Test
    void getDashboard_setsOccupiedBeds_fromPatientCount() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward()));
        when(patientRepository.findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(HOSPITAL_ID, WARD_ID))
                .thenReturn(List.of(patient("p-1", AcuityColor.GREEN), patient("p-2", AcuityColor.AMBER)));

        SupervisorDashboardResponse result = service.getDashboard(WARD_ID);

        assertThat(result.occupiedBeds()).isEqualTo(2);
    }

    @Test
    void getDashboard_usesHospitalIdFromContextHolder() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward()));
        when(patientRepository.findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(HOSPITAL_ID, WARD_ID))
                .thenReturn(List.of());

        service.getDashboard(WARD_ID);

        verify(wardRepository).findByIdAndHospitalId(WARD_ID, HOSPITAL_ID);
        verify(patientRepository).findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(HOSPITAL_ID, WARD_ID);
    }

    @Test
    void getDashboard_includesWardName_andSpecialty() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward()));
        when(patientRepository.findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(any(), any()))
                .thenReturn(List.of());

        SupervisorDashboardResponse result = service.getDashboard(WARD_ID);

        assertThat(result.wardName()).isEqualTo("Test Ward");
        assertThat(result.specialty()).isEqualTo("Cardiology");
    }

    @Test
    void getDashboard_includesWardTotalBeds() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward()));
        when(patientRepository.findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(any(), any()))
                .thenReturn(List.of());

        SupervisorDashboardResponse result = service.getDashboard(WARD_ID);

        assertThat(result.totalBeds()).isEqualTo(20);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Ward ward() {
        Ward w = new Ward();
        w.setId(WARD_ID);
        w.setHospitalId(HOSPITAL_ID);
        w.setName("Test Ward");
        w.setSpecialty("Cardiology");
        w.setTotalBeds(20);
        w.setActive(true);
        return w;
    }

    private Patient patient(String id, AcuityColor acuityColor) {
        Patient p = new Patient();
        p.setId(id);
        p.setHospitalId(HOSPITAL_ID);
        p.setWardId(WARD_ID);
        p.setFirstName("Jane");
        p.setLastName("Doe");
        p.setDateOfBirth(LocalDate.of(1990, 1, 1));
        p.setHospitalNumber("HN-" + id);
        p.setAdmissionDate(LocalDateTime.now());
        p.setAdmissionType(AdmissionType.EMERGENCY);
        p.setAcuityColor(acuityColor);
        p.setStatus(PatientStatus.ADMITTED);
        return p;
    }
}
