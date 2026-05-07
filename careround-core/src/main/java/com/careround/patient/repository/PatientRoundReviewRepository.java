package com.careround.patient.repository;

import com.careround.patient.entity.PatientRoundReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRoundReviewRepository extends JpaRepository<PatientRoundReview, String> {

    List<PatientRoundReview> findAllByRoundIdOrderByReviewOrder(String roundId);

    Optional<PatientRoundReview> findByRoundIdAndPatientId(String roundId, String patientId);
}
