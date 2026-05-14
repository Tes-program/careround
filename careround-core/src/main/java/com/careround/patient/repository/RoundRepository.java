package com.careround.patient.repository;

import com.careround.patient.entity.Round;
import com.careround.patient.enums.RoundStatus;
import com.careround.patient.enums.RoundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface RoundRepository extends JpaRepository<Round, String> {

    List<Round> findAllByWardIdAndMedicalTeamIdOrderByCreatedAtDesc(String wardId, String teamId);

    List<Round> findAllByHospitalIdAndWardIdAndStatusOrderByStartedAtDesc(
            String hospitalId, String wardId, RoundStatus status);

    boolean existsByWardIdAndMedicalTeamIdAndRoundTypeAndStatus(
            String wardId, String teamId, RoundType type, RoundStatus status);

    long countByHospitalIdAndStatus(String hospitalId, RoundStatus status);

    List<Round> findAllByHospitalIdOrderByCreatedAtDesc(String hospitalId);

    @Query("""
            select r
            from Round r
            where r.hospitalId = :hospitalId
              and (:wardId is null or r.wardId = :wardId)
              and coalesce(r.startedAt, r.scheduledTime, r.createdAt) between :from and :to
            order by coalesce(r.startedAt, r.scheduledTime, r.createdAt) desc
            """)
    List<Round> findRoundsForReport(
            @Param("hospitalId") String hospitalId,
            @Param("wardId") String wardId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
