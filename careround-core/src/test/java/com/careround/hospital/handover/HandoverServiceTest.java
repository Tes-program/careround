package com.careround.hospital.handover;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.Handover;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.HandoverStatus;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.handover.dto.AddPatientHandoverNoteRequest;
import com.careround.hospital.handover.dto.CompleteHandoverRequest;
import com.careround.hospital.handover.dto.HandoverResponse;
import com.careround.hospital.handover.dto.InitiateHandoverRequest;
import com.careround.hospital.repository.HandoverRepository;
import com.careround.hospital.repository.PatientHandoverNoteRepository;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandoverServiceTest {

    @Mock private HandoverRepository handoverRepository;
    @Mock private PatientHandoverNoteRepository patientHandoverNoteRepository;
    @Mock private WardRepository wardRepository;
    @Mock private ShiftRepository shiftRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private HandoverServiceImpl handoverService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String WARD_ID = "ward-1";
    private static final String OUTGOING_SHIFT_ID = "shift-out";
    private static final String INCOMING_SHIFT_ID = "shift-in";
    private static final String HANDOVER_ID = "hov-1";

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-1", UserRole.NURSE);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void initiateHandover_happyPath_createsHandover() {
        Ward ward = ward(WARD_ID, HOSPITAL_ID);
        Shift outgoing = shift(OUTGOING_SHIFT_ID, WARD_ID);
        Shift incoming = shift(INCOMING_SHIFT_ID, WARD_ID);

        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(handoverRepository.findByOutgoingShiftId(OUTGOING_SHIFT_ID)).thenReturn(Optional.empty());
        when(shiftRepository.findById(OUTGOING_SHIFT_ID)).thenReturn(Optional.of(outgoing));
        when(shiftRepository.findById(INCOMING_SHIFT_ID)).thenReturn(Optional.of(incoming));
        when(handoverRepository.save(any())).thenAnswer(inv -> {
            Handover h = inv.getArgument(0);
            h.setId(HANDOVER_ID);
            return h;
        });

        HandoverResponse result = handoverService.initiateHandover(
                new InitiateHandoverRequest(WARD_ID, OUTGOING_SHIFT_ID, INCOMING_SHIFT_ID, "Night handover"));

        assertThat(result.id()).isEqualTo(HANDOVER_ID);
        assertThat(result.status()).isEqualTo(HandoverStatus.PENDING);
    }

    @Test
    void initiateHandover_wardWrongHospital_throwsResourceNotFoundException() {
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handoverService.initiateHandover(
                new InitiateHandoverRequest(WARD_ID, OUTGOING_SHIFT_ID, INCOMING_SHIFT_ID, null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(handoverRepository, never()).save(any());
    }

    @Test
    void initiateHandover_duplicateHandoverForShift_throwsBusinessRuleException() {
        Ward ward = ward(WARD_ID, HOSPITAL_ID);
        Handover existing = handover(HANDOVER_ID, WARD_ID, OUTGOING_SHIFT_ID, HandoverStatus.PENDING);

        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(handoverRepository.findByOutgoingShiftId(OUTGOING_SHIFT_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> handoverService.initiateHandover(
                new InitiateHandoverRequest(WARD_ID, OUTGOING_SHIFT_ID, INCOMING_SHIFT_ID, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void addPatientHandoverNote_completedHandover_throwsBusinessRuleException() {
        Handover handover = handover(HANDOVER_ID, WARD_ID, OUTGOING_SHIFT_ID, HandoverStatus.COMPLETED);
        Ward ward = ward(WARD_ID, HOSPITAL_ID);

        when(handoverRepository.findById(HANDOVER_ID)).thenReturn(Optional.of(handover));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));

        assertThatThrownBy(() -> handoverService.addPatientHandoverNote(HANDOVER_ID,
                new AddPatientHandoverNoteRequest("p-1", "Stable", null, false)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("completed handover");
    }

    @Test
    void completeHandover_happyPath_updatesShiftAndPublishesEvent() {
        Handover handover = handover(HANDOVER_ID, WARD_ID, OUTGOING_SHIFT_ID, HandoverStatus.PENDING);
        Ward ward = ward(WARD_ID, HOSPITAL_ID);
        Shift outgoing = shift(OUTGOING_SHIFT_ID, WARD_ID);

        when(handoverRepository.findById(HANDOVER_ID)).thenReturn(Optional.of(handover));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));
        when(shiftRepository.findById(OUTGOING_SHIFT_ID)).thenReturn(Optional.of(outgoing));
        when(handoverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HandoverResponse result = handoverService.completeHandover(HANDOVER_ID,
                new CompleteHandoverRequest(null));

        assertThat(result.status()).isEqualTo(HandoverStatus.COMPLETED);
        assertThat(outgoing.getStatus()).isEqualTo(ShiftStatus.HANDED_OVER);
        verify(outboxService).publish(eq("careround.handover.completed"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void completeHandover_alreadyCompleted_throwsBusinessRuleException() {
        Handover handover = handover(HANDOVER_ID, WARD_ID, OUTGOING_SHIFT_ID, HandoverStatus.COMPLETED);
        Ward ward = ward(WARD_ID, HOSPITAL_ID);

        when(handoverRepository.findById(HANDOVER_ID)).thenReturn(Optional.of(handover));
        when(wardRepository.findByIdAndHospitalId(WARD_ID, HOSPITAL_ID)).thenReturn(Optional.of(ward));

        assertThatThrownBy(() -> handoverService.completeHandover(HANDOVER_ID,
                new CompleteHandoverRequest(null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already completed");
    }

    private Ward ward(String id, String hospitalId) {
        Ward w = new Ward();
        w.setId(id);
        w.setHospitalId(hospitalId);
        return w;
    }

    private Shift shift(String id, String wardId) {
        Shift s = new Shift();
        s.setId(id);
        s.setWardId(wardId);
        s.setStatus(ShiftStatus.ACTIVE);
        return s;
    }

    private Handover handover(String id, String wardId, String outgoingShiftId, HandoverStatus status) {
        Handover h = new Handover();
        h.setId(id);
        h.setWardId(wardId);
        h.setOutgoingShiftId(outgoingShiftId);
        h.setIncomingShiftId(INCOMING_SHIFT_ID);
        h.setConductedById("user-1");
        h.setStatus(status);
        return h;
    }
}
