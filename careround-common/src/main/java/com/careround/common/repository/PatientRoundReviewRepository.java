package com.careround.common.repository;

import com.careround.common.entity.PatientRoundReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRoundReviewRepository extends JpaRepository<PatientRoundReview, String> {
    List<PatientRoundReview> findByRoundIdOrderByReviewOrder(String roundId);
    List<PatientRoundReview> findByPatientIdOrderByReviewedAtDesc(String patientId);
    Optional<PatientRoundReview> findByRoundIdAndPatientId(String roundId, String patientId);
}
