package com.careround.scheduler.service;

import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.shared.event.MedicationTaskOverdueEvent;
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
public class MedicationTaskOverdueProcessor {

    private static final int PAGE_SIZE = 200;

    private final MedicationTaskRepository medicationTaskRepository;
    private final MedicationChartRepository medicationChartRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final OutboxService outboxService;

    @Transactional
    public int processOverdueTasks() {
        int totalProcessed = 0;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Page<MedicationTask> page;

        do {
            page = medicationTaskRepository.findAllByStatusAndScheduledTimeBeforeAndReminderSentAtIsNull(
                    MedicationTaskStatus.PENDING, now, PageRequest.of(0, PAGE_SIZE));

            for (MedicationTask task : page.getContent()) {
                processTask(task, now);
                totalProcessed++;
            }
        } while (page.hasNext());

        log.info("action=OVERDUE_PROCESSED count={}", totalProcessed);
        return totalProcessed;
    }

    private void processTask(MedicationTask task, LocalDateTime now) {
        String hospitalId = task.getHospitalId();

        MedicationChart chart = medicationChartRepository.findByIdAndHospitalId(task.getMedicationChartId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Chart not found: " + task.getMedicationChartId()));

        Prescription prescription = prescriptionRepository.findByIdAndHospitalId(chart.getPrescriptionId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + chart.getPrescriptionId()));

        task.setStatus(MedicationTaskStatus.OVERDUE);
        task.setReminderSentAt(now);
        medicationTaskRepository.save(task);

        long minutesOverdue = ChronoUnit.MINUTES.between(task.getScheduledTime(), now);

        outboxService.publish("medication-task-overdue",
                new MedicationTaskOverdueEvent(
                        UUID.randomUUID().toString(),
                        task.getId(),
                        task.getPatientId(),
                        task.getWardId(),
                        hospitalId,
                        task.getAssignedNurseId(),
                        prescription.getDrugName(),
                        prescription.getDose(),
                        task.getScheduledTime(),
                        minutesOverdue,
                        MDC.get("correlationId"),
                        now),
                hospitalId);
    }
}
