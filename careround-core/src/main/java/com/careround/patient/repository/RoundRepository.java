package com.careround.patient.repository;

import com.careround.patient.entity.Round;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.RoundType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoundRepository extends JpaRepository<Round, String> {

    List<Round> findAllByWardIdAndMedicalTeamIdOrderByCreatedAtDesc(String wardId, String teamId);

    boolean existsByWardIdAndMedicalTeamIdAndRoundTypeAndStatus(
            String wardId, String teamId, RoundType type, RoundStatus status);

    long countByHospitalIdAndStatus(String hospitalId, RoundStatus status);
}
