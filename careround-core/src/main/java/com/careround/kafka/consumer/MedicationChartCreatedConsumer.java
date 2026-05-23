package com.careround.kafka.consumer;

import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.shared.event.MedicationChartCreatedEvent;
import com.careround.shared.event.ProcessedEvent;
import com.careround.shared.event.ProcessedEventRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationChartCreatedConsumer {

    private final ProcessedEventRepository processedEventRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicationTaskRepository medicationTaskRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "medication-chart-created", groupId = "careround-core-internal",
            containerFactory = "internalKafkaListenerContainerFactory")
    @Transactional
    public void consume(ConsumerRecord<String, String> record) {
        MedicationChartCreatedEvent event = deserialize(record.value());

        if (processedEventRepository.existsById(event.eventId())) {
            log.info("action=SKIP_DUPLICATE eventId={} topic=medication-chart-created", event.eventId());
            return;
        }

        String hospitalId = event.hospitalId();

        Prescription prescription = prescriptionRepository.findByIdAndHospitalId(event.prescriptionId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + event.prescriptionId()));

        Map<String, Long> nursePendingCounts = loadNursePendingCounts(event.hospitalId(), event.wardId());

        List<MedicationTask> tasks = prescription.getAdministrationTimes().stream()
                .map(time -> buildTask(event, time, nursePendingCounts))
                .toList();

        medicationTaskRepository.saveAll(tasks);

        ProcessedEvent pe = new ProcessedEvent();
        pe.setEventId(event.eventId());
        pe.setProcessedAt(LocalDateTime.now(ZoneOffset.UTC));
        processedEventRepository.save(pe);

        log.info("action=TASKS_CREATED chartId={} prescriptionId={} count={}",
                event.medicationChartId(), event.prescriptionId(), tasks.size());
    }

    private MedicationTask buildTask(MedicationChartCreatedEvent event, LocalDateTime scheduledTime,
                                     Map<String, Long> nursePendingCounts) {
        MedicationTask task = new MedicationTask();
        task.setMedicationChartId(event.medicationChartId());
        task.setPatientId(event.patientId());
        task.setHospitalId(event.hospitalId());
        task.setWardId(event.wardId());
        task.setScheduledTime(scheduledTime);
        String nurseId = pickLeastLoadedNurse(nursePendingCounts);
        task.setAssignedNurseId(nurseId);
        if (nurseId != null) nursePendingCounts.merge(nurseId, 1L, Long::sum);
        return task;
    }

    private Map<String, Long> loadNursePendingCounts(String hospitalId, String wardId) {
        if (wardId == null) return new HashMap<>();
        Map<String, Long> counts = new HashMap<>();
        userRepository.findAllByHospitalIdAndRoleAndWardIdAndIsActiveTrue(hospitalId, UserRole.NURSE, wardId)
                .forEach(nurse -> counts.put(nurse.getId(),
                        medicationTaskRepository.countByAssignedNurseIdAndHospitalIdAndStatus(
                                nurse.getId(), hospitalId, MedicationTaskStatus.PENDING)));
        return counts;
    }

    private String pickLeastLoadedNurse(Map<String, Long> nursePendingCounts) {
        return nursePendingCounts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private MedicationChartCreatedEvent deserialize(String json) {
        try {
            return objectMapper.readValue(json, MedicationChartCreatedEvent.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize MedicationChartCreatedEvent", e);
        }
    }
}
