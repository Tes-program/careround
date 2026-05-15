package com.careround.patient.round;

import com.careround.patient.round.dto.CreateRoundRequest;
import com.careround.patient.round.dto.PatientRoundReviewResponse;
import com.careround.patient.round.dto.ReviewPatientRequest;
import com.careround.patient.round.dto.RoundResponse;

import java.util.List;

public interface RoundService {
    RoundResponse createRound(CreateRoundRequest request);
    RoundResponse startRound(String roundId);
    PatientRoundReviewResponse reviewPatient(String roundId, String patientId, ReviewPatientRequest request);
    RoundResponse completeRound(String roundId);
    RoundResponse cancelRound(String roundId);
    List<RoundResponse> getRounds(String wardId, String teamId);
    List<PatientRoundReviewResponse> getReviews(String roundId);
}
