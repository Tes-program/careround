package com.careround.patient.round.repository;

import com.careround.patient.round.entity.Round;
import com.careround.shared.enums.RoundStatus;
import com.careround.shared.enums.RoundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundRepository extends JpaRepository<Round, String> {
    boolean existsByHospitalIdAndWardIdAndRoundTypeAndStatus(String hospitalId, String wardId, RoundType roundType, RoundStatus status);
}
