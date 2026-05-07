package com.careround.patient.round.repository;

import com.careround.patient.round.entity.PatientRoundReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRoundReviewRepository extends JpaRepository<PatientRoundReview, String> {
}

