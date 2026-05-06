package com.careround.common.repository;

import com.careround.common.entity.Round;
import com.careround.common.enums.RoundStatus;
import com.careround.common.enums.RoundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, String> {

    /**
     * Business rule: Only one MORNING round may be IN_PROGRESS per ward+team per calendar day.
     */
    @Query("SELECT COUNT(r) > 0 FROM Round r WHERE r.wardId = :wardId " +
           "AND r.medicalTeamId = :teamId AND r.roundType = 'MORNING' " +
           "AND r.status = 'IN_PROGRESS' " +
           "AND CAST(r.createdAt AS LocalDate) = :today")
    boolean existsMorningRoundInProgressToday(
            @Param("wardId") String wardId,
            @Param("teamId") String teamId,
            @Param("today") LocalDate today);

    List<Round> findByWardIdOrderByCreatedAtDesc(String wardId);
    List<Round> findByMedicalTeamIdAndStatus(String medicalTeamId, RoundStatus status);
    List<Round> findByShiftId(String shiftId);
    Optional<Round> findByIdAndWardId(String id, String wardId);
}
