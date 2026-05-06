package com.careround.common.repository;

import com.careround.common.entity.Escalation;
import com.careround.common.enums.EscalationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface EscalationRepository extends JpaRepository<Escalation, String> {
    List<Escalation> findByHospitalIdAndStatus(String hospitalId, EscalationStatus status);
    List<Escalation> findByPatientIdOrderByCreatedAtDesc(String patientId);

    @Query("SELECT e FROM Escalation e " +
           "JOIN Patient p ON p.id = e.patientId " +
           "WHERE p.wardId = :wardId AND e.status = 'OPEN'")
    List<Escalation> findOpenByWardId(@Param("wardId") String wardId);

    /**
     * Used by EscalationWatcherJob: open escalations unacknowledged past the grace period.
     */
    @Query("SELECT e FROM Escalation e WHERE e.status = 'OPEN' " +
           "AND e.createdAt < :cutoff")
    List<Escalation> findUnacknowledgedOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
