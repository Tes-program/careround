package com.careround.common.repository;

import com.careround.common.entity.CareTask;
import com.careround.common.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CareTaskRepository extends JpaRepository<CareTask, String> {
    List<CareTask> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<CareTask> findByWardIdAndStatus(String wardId, TaskStatus status);
    List<CareTask> findByWardIdOrderByPriorityDescCreatedAtAsc(String wardId);
    List<CareTask> findByRoundId(String roundId);
    List<CareTask> findByAssignedToId(String assignedToId);

    /**
     * Used by TaskOverdueDetectorJob: find tasks past their window whose status
     * is still PENDING or IN_PROGRESS (i.e. have not been completed or cancelled).
     */
    @Query("SELECT t FROM CareTask t " +
           "JOIN Patient p ON p.id = t.patientId " +
           "WHERE p.hospitalId = :hospitalId " +
           "AND t.windowEnd < :now " +
           "AND t.status IN ('PENDING', 'IN_PROGRESS')")
    List<CareTask> findOverdueByHospitalId(
            @Param("hospitalId") String hospitalId,
            @Param("now") LocalDateTime now);

    /** Tasks pending for handover note inclusion */
    @Query("SELECT t FROM CareTask t WHERE t.wardId = :wardId " +
           "AND t.status IN ('PENDING', 'IN_PROGRESS') " +
           "ORDER BY t.priority DESC")
    List<CareTask> findPendingByWardId(@Param("wardId") String wardId);

    /** Check if all discharge tasks for a patient are complete */
    @Query("SELECT COUNT(t) FROM CareTask t WHERE t.patientId = :patientId " +
           "AND t.source = 'POST_ROUND_JOB' " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countIncompleteDischargeTasksByPatientId(@Param("patientId") String patientId);
}
