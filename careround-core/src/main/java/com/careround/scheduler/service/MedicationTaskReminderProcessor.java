package com.careround.scheduler.service;

import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.shared.event.MedicationTaskOverdueEvent;
import com.careround.shared.event.MedicationTaskReminderEvent;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationTaskReminderProcessor {

    private static final int PAGE_SIZE = 200;
    private static final int PRE_REMINDER_WINDOW_START_MINUTES = 4;
    private static final int PRE_REMINDER_WINDOW_END_MINUTES = 6;
    private static final int OVERDUE_THRESHOLD_MINUTES = 5;

    private final MedicationTaskRepository medicationTaskRepository;
    private final MedicationChartRepository medicationChartRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final OutboxService outboxService;

    @Transactional
    public int processReminders() {
        int total = 0;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime windowStart = now.plusMinutes(PRE_REMINDER_WINDOW_START_MINUTES);
        LocalDateTime windowEnd = now.plusMinutes(PRE_REMINDER_WINDOW_END_MINUTES);

        Page<MedicationTask> page;
        do {
            page = medicationTaskRepository
                    .findAllByStatusAndScheduledTimeBetweenAndPreReminderSentAtIsNull(
                            MedicationTaskStatus.PENDING, windowStart, windowEnd, PageRequest.of(0, PAGE_SIZE));
            for (MedicationTask task : page.getContent()) {
                sendPreReminder(task, now);
                total++;
            }
        } while (page.hasNext());

        log.info("action=PRE_REMINDER_PROCESSED count={}", total);
        return total;
    }

    @Transactional
    public int processOverdue() {
        int total = 0;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime overdueThreshold = now.minusMinutes(OVERDUE_THRESHOLD_MINUTES);

        Page<MedicationTask> page;
        do {
            page = medicationTaskRepository
                    .findAllByStatusAndScheduledTimeBeforeAndOverdueAlertSentAtIsNull(
                            MedicationTaskStatus.PENDING, overdueThreshold, PageRequest.of(0, PAGE_SIZE));
            for (MedicationTask task : page.getContent()) {
                markOverdue(task, now);
                total++;
            }
        } while (page.hasNext());

        log.info("action=OVERDUE_PROCESSED count={}", total);
        return total;
    }

    private void sendPreReminder(MedicationTask task, LocalDateTime now) {
        String hospitalId = task.getHospitalId();
        Prescription prescription = lookupPrescription(task, hospitalId);

        task.setPreReminderSentAt(now);
        medicationTaskRepository.save(task);

        outboxService.publish("medication-task-reminder",
                new MedicationTaskReminderEvent(UUID.randomUUID().toString(),
                        task.getId(), task.getPatientId(), task.getWardId(), hospitalId,
                        task.getAssignedNurseId(), prescription.getDrugName(), prescription.getDose(),
                        task.getScheduledTime(), MDC.get("correlationId"), now),
                hospitalId);
    }

    private void markOverdue(MedicationTask task, LocalDateTime now) {
        String hospitalId = task.getHospitalId();
        Prescription prescription = lookupPrescription(task, hospitalId);

        task.setStatus(MedicationTaskStatus.OVERDUE);
        task.setOverdueAlertSentAt(now);
        medicationTaskRepository.save(task);

        long minutesOverdue = ChronoUnit.MINUTES.between(task.getScheduledTime(), now);

        outboxService.publish("medication-task-overdue",
                new MedicationTaskOverdueEvent(UUID.randomUUID().toString(),
                        task.getId(), task.getPatientId(), task.getWardId(), hospitalId,
                        task.getAssignedNurseId(), prescription.getDrugName(), prescription.getDose(),
                        task.getScheduledTime(), minutesOverdue, MDC.get("correlationId"), now),
                hospitalId);
    }

    private Prescription lookupPrescription(MedicationTask task, String hospitalId) {
        MedicationChart chart = medicationChartRepository
                .findByIdAndHospitalId(task.getMedicationChartId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Chart not found: " + task.getMedicationChartId()));
        return prescriptionRepository
                .findByIdAndHospitalId(chart.getPrescriptionId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + chart.getPrescriptionId()));
    }
}
