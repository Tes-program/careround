package com.careround.patient.round;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientRoundReview;
import com.careround.patient.entity.Round;
import com.careround.patient.enums.ClinicalStatus;
import com.careround.patient.enums.DischargeAssessment;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.RoundType;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientRoundReviewRepository;
import com.careround.patient.repository.RoundRepository;
import com.careround.patient.round.dto.CreateRoundRequest;
import com.careround.patient.round.dto.PatientRoundReviewResponse;
import com.careround.patient.round.dto.ReviewPatientRequest;
import com.careround.patient.round.dto.RoundResponse;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {

    @Mock private RoundRepository roundRepository;
    @Mock private PatientRoundReviewRepository patientRoundReviewRepository;
    @Mock private WardRepository wardRepository;
    @Mock private MedicalTeamRepository medicalTeamRepository;
    @Mock private ShiftRepository shiftRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private RoundServiceImpl roundService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String WARD_ID = "ward-1";
    private static final String TEAM_ID = "team-1";
    private static final String SHIFT_ID = "shift-1";
    private static final String ROUND_ID = "round-1";
    private static final String LEAD_DOC = "doc-1";

    @BeforeEach
    void setUp() {
        HospitalContextHolder.set(HOSPITAL_ID, "user-1", UserRole.CONSULTANT);
    }

    @AfterEach
    void tearDown() {
        HospitalContextHolder.clear();
    }

    @Test
    void createRound_happyPath_returnsSavedRound() {
        Ward ward = ward(WARD_ID, HOSPITAL_ID);
        MedicalTeam team = team(TEAM_ID, HOSPITAL_ID);
        Shift shift = shift(SHIFT_ID, WARD_ID);

        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(ward));
        when(medicalTeamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(roundRepository.existsByWardIdAndMedicalTeamIdAndRoundTypeAndStatus(
                WARD_ID, TEAM_ID, RoundType.MORNING, RoundStatus.IN_PROGRESS)).thenReturn(false);
        when(shiftRepository.findFirstByWardIdAndStatusOrderByStartTimeDesc(WARD_ID, ShiftStatus.ACTIVE))
                .thenReturn(Optional.of(shift));
        when(roundRepository.save(any())).thenAnswer(inv -> {
            Round r = inv.getArgument(0);
            r.setId(ROUND_ID);
            return r;
        });

        RoundResponse result = roundService.createRound(new CreateRoundRequest(
                WARD_ID, TEAM_ID, RoundType.MORNING, LEAD_DOC, null, null));

        assertThat(result.id()).isEqualTo(ROUND_ID);
        assertThat(result.status()).isEqualTo(RoundStatus.SCHEDULED);
    }

    @Test
    void createRound_wardWrongHospital_throwsAccessDeniedException() {
        Ward ward = ward(WARD_ID, "other-hospital");
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(ward));

        assertThatThrownBy(() -> roundService.createRound(
                new CreateRoundRequest(WARD_ID, TEAM_ID, RoundType.MORNING, LEAD_DOC, null, null)))
                .isInstanceOf(AccessDeniedException.class);

        verify(roundRepository, never()).save(any());
    }

    @Test
    void createRound_duplicateInProgressRound_throwsBusinessRuleException() {
        Ward ward = ward(WARD_ID, HOSPITAL_ID);
        MedicalTeam team = team(TEAM_ID, HOSPITAL_ID);
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(ward));
        when(medicalTeamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(roundRepository.existsByWardIdAndMedicalTeamIdAndRoundTypeAndStatus(
                WARD_ID, TEAM_ID, RoundType.MORNING, RoundStatus.IN_PROGRESS)).thenReturn(true);

        assertThatThrownBy(() -> roundService.createRound(
                new CreateRoundRequest(WARD_ID, TEAM_ID, RoundType.MORNING, LEAD_DOC, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("active round");
    }

    @Test
    void createRound_noActiveShift_throwsBusinessRuleException() {
        Ward ward = ward(WARD_ID, HOSPITAL_ID);
        MedicalTeam team = team(TEAM_ID, HOSPITAL_ID);
        when(wardRepository.findById(WARD_ID)).thenReturn(Optional.of(ward));
        when(medicalTeamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(roundRepository.existsByWardIdAndMedicalTeamIdAndRoundTypeAndStatus(any(), any(), any(), any()))
                .thenReturn(false);
        when(shiftRepository.findFirstByWardIdAndStatusOrderByStartTimeDesc(WARD_ID, ShiftStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> roundService.createRound(
                new CreateRoundRequest(WARD_ID, TEAM_ID, RoundType.MORNING, LEAD_DOC, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No active shift");
    }

    @Test
    void startRound_happyPath_populatesPatientReviews() {
        Round round = round(ROUND_ID, HOSPITAL_ID, WARD_ID, RoundStatus.SCHEDULED);
        Patient patient1 = patient("p-1", HOSPITAL_ID, WARD_ID);
        Patient patient2 = patient("p-2", HOSPITAL_ID, WARD_ID);

        when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));
        when(patientRepository.findAllByHospitalIdAndWardIdAndStatusOrderByNewsScoreDescAdmissionDateAsc(
                HOSPITAL_ID, WARD_ID, PatientStatus.ADMITTED))
                .thenReturn(List.of(patient1, patient2));
        when(roundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PatientRoundReview>> captor = ArgumentCaptor.forClass(List.class);

        roundService.startRound(ROUND_ID);

        verify(patientRoundReviewRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue().get(0).getReviewOrder()).isEqualTo(1);
        assertThat(captor.getValue().get(0).getClinicalStatus()).isEqualTo(ClinicalStatus.STABLE);
    }

    @Test
    void startRound_notScheduled_throwsBusinessRuleException() {
        Round round = round(ROUND_ID, HOSPITAL_ID, WARD_ID, RoundStatus.IN_PROGRESS);
        when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));

        assertThatThrownBy(() -> roundService.startRound(ROUND_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("SCHEDULED");
    }

    @Test
    void reviewPatient_happyPath_updatesReviewedById() {
        Round round = round(ROUND_ID, HOSPITAL_ID, WARD_ID, RoundStatus.IN_PROGRESS);
        PatientRoundReview review = review(ROUND_ID, "p-1", 1);
        Patient patient = patient("p-1", HOSPITAL_ID, WARD_ID);

        when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));
        when(patientRoundReviewRepository.findByRoundIdAndPatientId(ROUND_ID, "p-1"))
                .thenReturn(Optional.of(review));
        when(patientRepository.findByIdAndHospitalId("p-1", HOSPITAL_ID))
                .thenReturn(Optional.of(patient));
        when(patientRoundReviewRepository.save(any())).thenAnswer(inv -> {
            PatientRoundReview r = inv.getArgument(0);
            r.setId("rev-1");
            return r;
        });

        PatientRoundReviewResponse result = roundService.reviewPatient(ROUND_ID, "p-1",
                new ReviewPatientRequest(ClinicalStatus.IMPROVING, true, "Plan", DischargeAssessment.POSSIBLE, false));

        assertThat(result.clinicalStatus()).isEqualTo(ClinicalStatus.IMPROVING);
        assertThat(result.wasExamined()).isTrue();
        assertThat(review.getReviewedById()).isEqualTo("user-1");
    }

    @Test
    void reviewPatient_roundNotInProgress_throwsBusinessRuleException() {
        Round round = round(ROUND_ID, HOSPITAL_ID, WARD_ID, RoundStatus.SCHEDULED);
        when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));

        assertThatThrownBy(() -> roundService.reviewPatient(ROUND_ID, "p-1",
                new ReviewPatientRequest(ClinicalStatus.STABLE, false, null, DischargeAssessment.NONE, false)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not in progress");
    }

    @Test
    void completeRound_happyPath_publishesRoundCompletedEvent() {
        Round round = round(ROUND_ID, HOSPITAL_ID, WARD_ID, RoundStatus.IN_PROGRESS);
        when(roundRepository.findById(ROUND_ID)).thenReturn(Optional.of(round));
        when(roundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RoundResponse result = roundService.completeRound(ROUND_ID);

        assertThat(result.status()).isEqualTo(RoundStatus.COMPLETED);
        verify(outboxService).publish(eq("careround.round.completed"), any(), eq(HOSPITAL_ID));
    }

    private Ward ward(String id, String hospitalId) {
        Ward w = new Ward();
        w.setId(id);
        w.setHospitalId(hospitalId);
        return w;
    }

    private MedicalTeam team(String id, String hospitalId) {
        MedicalTeam t = new MedicalTeam();
        t.setId(id);
        t.setHospitalId(hospitalId);
        return t;
    }

    private Shift shift(String id, String wardId) {
        Shift s = new Shift();
        s.setId(id);
        s.setWardId(wardId);
        s.setStatus(ShiftStatus.ACTIVE);
        return s;
    }

    private Round round(String id, String hospitalId, String wardId, RoundStatus status) {
        Round r = new Round();
        r.setId(id);
        r.setHospitalId(hospitalId);
        r.setWardId(wardId);
        r.setMedicalTeamId(TEAM_ID);
        r.setShiftId(SHIFT_ID);
        r.setLeadDoctorId(LEAD_DOC);
        r.setRoundType(RoundType.MORNING);
        r.setStatus(status);
        return r;
    }

    private Patient patient(String id, String hospitalId, String wardId) {
        Patient p = new Patient();
        p.setId(id);
        p.setHospitalId(hospitalId);
        p.setWardId(wardId);
        p.setStatus(PatientStatus.ADMITTED);
        p.setNewsScore(0);
        return p;
    }

    private PatientRoundReview review(String roundId, String patientId, int order) {
        PatientRoundReview r = new PatientRoundReview();
        r.setRoundId(roundId);
        r.setPatientId(patientId);
        r.setReviewedById(LEAD_DOC);
        r.setReviewOrder(order);
        r.setClinicalStatus(ClinicalStatus.STABLE);
        r.setDischargeAssessment(DischargeAssessment.NONE);
        return r;
    }
}
