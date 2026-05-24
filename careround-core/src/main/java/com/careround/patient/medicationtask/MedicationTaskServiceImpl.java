package com.careround.patient.medicationtask;

import com.careround.auth.entity.User;
import com.careround.auth.repository.UserRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationtask.dto.MedicationTaskResponse;
import com.careround.patient.medicationtask.dto.TaskListResponse;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.MedicationTaskCompletedEvent;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationTaskServiceImpl implements MedicationTaskService {

    private static final int DUE_SOON_MINUTES = 30;

    private final MedicationTaskRepository medicationTaskRepository;
    private final PatientRepository patientRepository;
    private final MedicationChartRepository medicationChartRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional(readOnly = true)
    public TaskListResponse getTaskList(String wardId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        List<MedicationTask> tasks = medicationTaskRepository.findAllByWardIdAndHospitalIdAndStatusIn(
                wardId, hospitalId, List.of(MedicationTaskStatus.PENDING, MedicationTaskStatus.OVERDUE));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        LocalDateTime dueSoonThreshold = now.plusMinutes(DUE_SOON_MINUTES);

        List<MedicationTask> completedTasks = medicationTaskRepository
                .findAllByWardIdAndHospitalIdAndStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                        wardId, hospitalId, MedicationTaskStatus.COMPLETED, startOfDay, endOfDay);

        // Batch-load enrichment data for all tasks (active + completed) to avoid N+1 queries
        List<MedicationTask> allTasks = Stream.concat(tasks.stream(), completedTasks.stream()).toList();

        Set<String> patientIds = allTasks.stream().map(MedicationTask::getPatientId).collect(Collectors.toSet());
        Set<String> chartIds = allTasks.stream().map(MedicationTask::getMedicationChartId).collect(Collectors.toSet());
        Set<String> completedByIds = allTasks.stream()
                .map(MedicationTask::getCompletedById).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<String, Patient> patients = patientRepository.findAllById(patientIds)
                .stream().collect(Collectors.toMap(Patient::getId, Function.identity()));

        Map<String, MedicationChart> charts = medicationChartRepository.findAllById(chartIds)
                .stream().collect(Collectors.toMap(MedicationChart::getId, Function.identity()));

        Set<String> prescriptionIds = charts.values().stream()
                .map(MedicationChart::getPrescriptionId).collect(Collectors.toSet());
        Map<String, Prescription> prescriptions = prescriptionRepository.findAllById(prescriptionIds)
                .stream().collect(Collectors.toMap(Prescription::getId, Function.identity()));

        Map<String, String> userNames = userRepository.findAllById(completedByIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u.getFirstName() + " " + u.getLastName()));

        List<MedicationTaskResponse> overdue = tasks.stream()
                .filter(t -> t.getStatus() == MedicationTaskStatus.OVERDUE)
                .map(t -> toEnrichedResponse(t, patients, charts, prescriptions, userNames, now))
                .toList();

        List<MedicationTaskResponse> dueSoon = tasks.stream()
                .filter(t -> t.getStatus() == MedicationTaskStatus.PENDING
                        && t.getScheduledTime().isBefore(dueSoonThreshold))
                .map(t -> toEnrichedResponse(t, patients, charts, prescriptions, userNames, now))
                .toList();

        List<MedicationTaskResponse> upcoming = tasks.stream()
                .filter(t -> t.getStatus() == MedicationTaskStatus.PENDING
                        && !t.getScheduledTime().isBefore(dueSoonThreshold))
                .map(t -> toEnrichedResponse(t, patients, charts, prescriptions, userNames, now))
                .toList();

        List<MedicationTaskResponse> completed = completedTasks.stream()
                .map(t -> toEnrichedResponse(t, patients, charts, prescriptions, userNames, now))
                .toList();

        return new TaskListResponse(overdue, dueSoon, upcoming, completed);
    }

    @Override
    @Transactional
    public void complete(String taskId, String actualDoseGiven) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        MedicationTask task = medicationTaskRepository.findByIdAndHospitalId(taskId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication task not found: " + taskId));

        if (task.getStatus() == MedicationTaskStatus.COMPLETED) {
            throw new IllegalStateException("Task is already completed: " + taskId);
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        task.setStatus(MedicationTaskStatus.COMPLETED);
        task.setCompletedAt(now);
        task.setCompletedById(userId);
        task.setActualDoseGiven(actualDoseGiven);
        medicationTaskRepository.save(task);

        outboxService.publish("medication-task-completed",
                new MedicationTaskCompletedEvent(UUID.randomUUID().toString(), taskId,
                        task.getMedicationChartId(), task.getPatientId(), task.getWardId(),
                        hospitalId, userId, actualDoseGiven, now, MDC.get("correlationId"), now),
                hospitalId);

        log.info("action=TASK_COMPLETED taskId={} completedBy={}", taskId, userId);
    }

    private MedicationTaskResponse toEnrichedResponse(MedicationTask t,
            Map<String, Patient> patients,
            Map<String, MedicationChart> charts,
            Map<String, Prescription> prescriptions,
            Map<String, String> userNames,
            LocalDateTime now) {
        Patient patient = patients.get(t.getPatientId());
        MedicationChart chart = charts.get(t.getMedicationChartId());
        Prescription prescription = chart != null ? prescriptions.get(chart.getPrescriptionId()) : null;

        Long minutesOverdue = null;
        if (t.getStatus() == MedicationTaskStatus.OVERDUE) {
            long diff = ChronoUnit.MINUTES.between(t.getScheduledTime(), now);
            minutesOverdue = diff > 0 ? diff : 0L;
        }

        return new MedicationTaskResponse(
                t.getId(), t.getMedicationChartId(), t.getPatientId(), t.getHospitalId(),
                t.getWardId(), t.getAssignedNurseId(), t.getScheduledTime(), t.getStatus(),
                t.getCompletedAt(), t.getCompletedById(), t.getActualDoseGiven(),
                t.getPreReminderSentAt(), t.getOverdueAlertSentAt(),
                t.getCreatedAt(), t.getUpdatedAt(),
                patient != null ? patient.getFirstName() : null,
                patient != null ? patient.getLastName() : null,
                patient != null ? patient.getBedNumber() : null,
                prescription != null ? prescription.getDrugName() : null,
                prescription != null ? prescription.getDose() : null,
                prescription != null ? prescription.getRoute() : null,
                minutesOverdue,
                t.getCompletedById() != null ? userNames.get(t.getCompletedById()) : null);
    }
}
