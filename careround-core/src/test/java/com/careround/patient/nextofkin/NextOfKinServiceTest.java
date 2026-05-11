package com.careround.patient.nextofkin;

import com.careround.auth.enums.UserRole;
import com.careround.patient.entity.NextOfKin;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.ContactMethod;
import com.careround.patient.nextofkin.dto.AddNextOfKinRequest;
import com.careround.patient.nextofkin.dto.NextOfKinResponse;
import com.careround.patient.repository.NextOfKinRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NextOfKinServiceTest {

    @Mock private NextOfKinRepository nextOfKinRepository;
    @Mock private PatientRepository patientRepository;

    @InjectMocks private NextOfKinServiceImpl nextOfKinService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String PATIENT_ID = "patient-1";

    private Patient patient;

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-nurse", UserRole.NURSE);

        patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void addNextOfKin_withEmergencyContact_clearsOtherEmergencyContacts() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));

        NextOfKin existing = new NextOfKin();
        existing.setId("nok-existing");
        existing.setPatientId(PATIENT_ID);
        existing.setEmergencyContact(true);
        when(nextOfKinRepository.findEmergencyContactsByPatientId(PATIENT_ID))
                .thenReturn(List.of(existing));
        when(nextOfKinRepository.save(any())).thenAnswer(inv -> {
            NextOfKin n = inv.getArgument(0);
            n.setId("nok-new");
            return n;
        });

        AddNextOfKinRequest request = new AddNextOfKinRequest(
                "Jane Doe", "Spouse", "07700000001", null,
                ContactMethod.SMS, true, true);
        NextOfKinResponse result = nextOfKinService.addNextOfKin(PATIENT_ID, request);

        assertThat(result.isEmergencyContact()).isTrue();
        assertThat(existing.isEmergencyContact()).isFalse();
    }

    @Test
    void addNextOfKin_crossTenantPatient_throwsAccessDeniedException() {
        Patient other = new Patient();
        other.setId(PATIENT_ID);
        other.setHospitalId("other-hosp");
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> nextOfKinService.addNextOfKin(PATIENT_ID,
                new AddNextOfKinRequest("Jane", "Spouse", "07700", null, ContactMethod.SMS, false, false)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void removeNextOfKin_nokBelongsToDifferentPatient_throwsNotFoundException() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        when(nextOfKinRepository.findByIdAndPatientId("nok-1", PATIENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> nextOfKinService.removeNextOfKin(PATIENT_ID, "nok-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeNextOfKin_happyPath_deletesNok() {
        when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
        NextOfKin nok = new NextOfKin();
        nok.setId("nok-1");
        nok.setPatientId(PATIENT_ID);
        when(nextOfKinRepository.findByIdAndPatientId("nok-1", PATIENT_ID)).thenReturn(Optional.of(nok));

        nextOfKinService.removeNextOfKin(PATIENT_ID, "nok-1");

        verify(nextOfKinRepository).delete(nok);
    }
}
