package com.careround.patient.medicationtask;

import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MedicationTaskRepository extends JpaRepository<MedicationTask, String> {

    Optional<MedicationTask> findByIdAndHospitalId(String id, String hospitalId);

    List<MedicationTask> findAllByMedicationChartId(String medicationChartId);

    List<MedicationTask> findAllByStatusAndScheduledTimeBefore(
            MedicationTaskStatus status, LocalDateTime threshold);

    List<MedicationTask> findAllByWardIdAndHospitalIdAndStatusIn(
            String wardId, String hospitalId, List<MedicationTaskStatus> statuses);

    List<MedicationTask> findAllByAssignedNurseIdAndHospitalIdAndStatusIn(
            String assignedNurseId, String hospitalId, List<MedicationTaskStatus> statuses);

    long countByWardIdAndHospitalIdAndStatus(
            String wardId, String hospitalId, MedicationTaskStatus status);

    long countByWardIdAndHospitalIdAndStatusAndCompletedAtBetween(
            String wardId, String hospitalId, MedicationTaskStatus status,
            LocalDateTime from, LocalDateTime to);

    List<MedicationTask> findAllByWardIdAndHospitalIdAndStatusAndScheduledTimeBetweenOrderByScheduledTimeAsc(
            String wardId, String hospitalId, MedicationTaskStatus status,
            LocalDateTime from, LocalDateTime to);

    List<MedicationTask> findAllByWardIdAndHospitalIdAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
            String wardId, String hospitalId, MedicationTaskStatus status,
            LocalDateTime from, LocalDateTime to);

    Page<MedicationTask> findAllByStatusAndScheduledTimeBetweenAndPreReminderSentAtIsNull(
            MedicationTaskStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<MedicationTask> findAllByStatusAndScheduledTimeBeforeAndOverdueAlertSentAtIsNull(
            MedicationTaskStatus status, LocalDateTime threshold, Pageable pageable);

    long countByAssignedNurseIdAndHospitalIdAndStatus(
            String assignedNurseId, String hospitalId, MedicationTaskStatus status);
}
