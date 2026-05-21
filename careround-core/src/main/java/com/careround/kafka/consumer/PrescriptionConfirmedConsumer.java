package com.careround.kafka.consumer;

import com.careround.patient.entity.Patient;
import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.MedicationChartCreatedEvent;
import com.careround.shared.event.PrescriptionConfirmedEvent;
import com.careround.shared.event.ProcessedEvent;
import com.careround.shared.event.ProcessedEventRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PrescriptionConfirmedConsumer {

    private final ProcessedEventRepository processedEventRepository;
    private final PatientRepository patientRepository;
    private final MedicationChartRepository medicationChartRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "prescription-confirmed", groupId = "careround-core-internal",
            containerFactory = "internalKafkaListenerContainerFactory")
    @Transactional
    public void consume(ConsumerRecord<String, String> record) {
        PrescriptionConfirmedEvent event = deserialize(record.value());

        if (processedEventRepository.existsById(event.eventId())) {
            log.info("action=SKIP_DUPLICATE eventId={} topic=prescription-confirmed", event.eventId());
            return;
        }

        String hospitalId = event.hospitalId();
        String patientId = event.patientId();

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));

        MedicationChart chart = new MedicationChart();
        chart.setPatientId(patientId);
        chart.setHospitalId(hospitalId);
        chart.setPrescriptionId(event.prescriptionId());
        MedicationChart savedChart = medicationChartRepository.save(chart);

        ProcessedEvent pe = new ProcessedEvent();
        pe.setEventId(event.eventId());
        pe.setProcessedAt(LocalDateTime.now(ZoneOffset.UTC));
        processedEventRepository.save(pe);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        outboxService.publish("medication-chart-created",
                new MedicationChartCreatedEvent(
                        UUID.randomUUID().toString(),
                        savedChart.getId(),
                        event.prescriptionId(),
                        patientId,
                        patient.getWardId(),
                        hospitalId,
                        MDC.get("correlationId"),
                        now),
                hospitalId);

        log.info("action=CHART_CREATED chartId={} prescriptionId={} patientId={}",
                savedChart.getId(), event.prescriptionId(), patientId);
    }

    private PrescriptionConfirmedEvent deserialize(String json) {
        try {
            return objectMapper.readValue(json, PrescriptionConfirmedEvent.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize PrescriptionConfirmedEvent", e);
        }
    }
}
