package com.careround.patient.repository;

import com.careround.patient.entity.CareTask;
import com.careround.patient.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CareTaskRepository extends JpaRepository<CareTask, String> {

    List<CareTask> findAllByHospitalIdAndWardIdAndStatusOrderByWindowEnd(
            String hospitalId, String wardId, TaskStatus status);

    List<CareTask> findAllByHospitalIdAndWardIdAndStatusInOrderByWindowEndAsc(
            String hospitalId, String wardId, List<TaskStatus> statuses);

    List<CareTask> findAllByPatientId(String patientId);

    List<CareTask> findAllByHospitalIdAndStatusInOrderByWindowEndAsc(String hospitalId, List<TaskStatus> statuses);

    List<CareTask> findAllByStatusInAndWindowEndBefore(List<TaskStatus> statuses, LocalDateTime now);

    List<CareTask> findAllByStatusInAndEscalatedAtIsNullAndWindowEndBefore(
            List<TaskStatus> statuses, LocalDateTime now);

    long countByHospitalIdAndStatusIn(String hospitalId, List<TaskStatus> statuses);

    long countByHospitalIdAndAssignedToIdAndStatusIn(String hospitalId, String assignedToId, List<TaskStatus> statuses);

    long countByHospitalIdAndWardIdInAndStatusIn(String hospitalId, List<String> wardIds, List<TaskStatus> statuses);

    List<CareTask> findAllByHospitalIdAndWardIdInAndStatusInOrderByWindowEndAsc(
            String hospitalId, List<String> wardIds, List<TaskStatus> statuses);

    long countByHospitalIdAndStatusInAndWindowEndBefore(String hospitalId, List<TaskStatus> statuses, LocalDateTime now);

    long countByHospitalIdAndWardIdInAndStatusInAndWindowEndBefore(
            String hospitalId, List<String> wardIds, List<TaskStatus> statuses, LocalDateTime now);

    boolean existsByHospitalIdAndAssignedToIdAndStatusInAndWindowStartLessThanAndWindowEndGreaterThan(
            String hospitalId,
            String assignedToId,
            List<TaskStatus> statuses,
            LocalDateTime windowEnd,
            LocalDateTime windowStart);

    @Query("""
            select count(t) > 0
            from CareTask t
            where t.hospitalId = :hospitalId
              and t.assignedToId = :assignedToId
              and t.id <> :excludedTaskId
              and t.status in :statuses
              and t.windowStart < :windowEnd
              and t.windowEnd > :windowStart
            """)
    boolean existsOverlappingAssignedTaskExcludingTask(
            @Param("hospitalId") String hospitalId,
            @Param("assignedToId") String assignedToId,
            @Param("excludedTaskId") String excludedTaskId,
            @Param("statuses") List<TaskStatus> statuses,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd);

    @Query("""
            select t
            from CareTask t
            where t.hospitalId = :hospitalId
              and (
                    lower(t.title) like lower(concat('%', :q, '%'))
                 or lower(t.description) like lower(concat('%', :q, '%'))
                 or lower(t.taskType) like lower(concat('%', :q, '%'))
              )
            order by t.createdAt desc
            """)
    List<CareTask> searchByHospitalId(@Param("hospitalId") String hospitalId, @Param("q") String q);

    @Query("""
            select t
            from CareTask t
            where t.hospitalId = :hospitalId
              and (:wardId is null or t.wardId = :wardId)
              and t.status = :status
              and t.completedAt between :from and :to
            order by t.completedAt asc
            """)
    List<CareTask> findReportTasks(
            @Param("hospitalId") String hospitalId,
            @Param("wardId") String wardId,
            @Param("status") TaskStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            select t
            from CareTask t
            where t.hospitalId = :hospitalId
              and (:wardId is null or t.wardId = :wardId)
              and (t.status = :overdueStatus
                   or (t.status in :activeStatuses and t.windowEnd < CURRENT_TIMESTAMP))
              and t.windowEnd between :from and :to
            order by t.windowEnd asc
            """)
    List<CareTask> findOverdueReportTasks(
            @Param("hospitalId") String hospitalId,
            @Param("wardId") String wardId,
            @Param("overdueStatus") TaskStatus overdueStatus,
            @Param("activeStatuses") List<TaskStatus> activeStatuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
