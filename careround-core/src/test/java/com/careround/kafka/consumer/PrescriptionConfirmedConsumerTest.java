package com.careround.kafka.consumer;

import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.MedicationChartCreatedEvent;
import com.careround.shared.event.PrescriptionConfirmedEvent;
import com.careround.shared.event.ProcessedEvent;
import com.careround.shared.event.ProcessedEventRepository;
import com.careround.shared.security.HospitalContextHolder;
import com.careround.shared.service.OutboxService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionConfirmedConsumerTest {

    @Mock private ProcessedEventRepository processedEventRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private MedicationChartRepository medicationChartRepository;
    @Mock private OutboxService outboxService;
    @Spy private ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @InjectMocks private PrescriptionConfirmedConsumer consumer;

    private static final String EVENT_ID = "evt-001";
    private static final String PRESCRIPTION_ID = "rx-001";
    private static final String PATIENT_ID = "patient-001";
    private static final String HOSPITAL_ID = "hosp-001";
    private static final String WARD_ID = "ward-001";

    @Test
    void consume_createsChart_andPublishesOutboxEvent() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patientWithWard(WARD_ID)));
        when(medicationChartRepository.save(any())).thenAnswer(inv -> {
            MedicationChart c = inv.getArgument(0);
            c.setId("chart-001");
            return c;
        });

        consumer.consume(record(event(EVENT_ID)));

        verify(medicationChartRepository).save(any(MedicationChart.class));
        verify(outboxService).publish(eq("medication-chart-created"), any(MedicationChartCreatedEvent.class), eq(HOSPITAL_ID));
    }

    @Test
    void consume_skipsProcessing_whenEventAlreadyProcessed() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(true);

        consumer.consume(record(event(EVENT_ID)));

        verify(medicationChartRepository, never()).save(any());
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void consume_throwsException_whenPatientNotFound() {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.consume(record(event(EVENT_ID))))
                .isInstanceOf(RuntimeException.class);

        verify(medicationChartRepository, never()).save(any());
    }

    @Test
    void consume_setsCorrectWardId_fromPatient() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patientWithWard("ward-specific")));
        when(medicationChartRepository.save(any())).thenAnswer(inv -> {
            MedicationChart c = inv.getArgument(0);
            c.setId("chart-001");
            return c;
        });

        ArgumentCaptor<MedicationChartCreatedEvent> captor = ArgumentCaptor.forClass(MedicationChartCreatedEvent.class);

        consumer.consume(record(event(EVENT_ID)));

        verify(outboxService).publish(any(), captor.capture(), any());
        assertThat(captor.getValue().wardId()).isEqualTo("ward-specific");
    }

    @Test
    void consume_savesProcessedEvent() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patientWithWard(WARD_ID)));
        when(medicationChartRepository.save(any())).thenAnswer(inv -> {
            MedicationChart c = inv.getArgument(0);
            c.setId("chart-001");
            return c;
        });

        consumer.consume(record(event(EVENT_ID)));

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(EVENT_ID);
    }

    @Test
    void consume_usesHospitalIdFromEvent_notContextHolder() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(patientRepository.findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(patientWithWard(WARD_ID)));
        when(medicationChartRepository.save(any())).thenAnswer(inv -> {
            MedicationChart c = inv.getArgument(0);
            c.setId("chart-001");
            return c;
        });

        HospitalContextHolder.clear();
        consumer.consume(record(event(EVENT_ID)));

        verify(patientRepository).findByIdAndHospitalId(PATIENT_ID, HOSPITAL_ID);
        verify(outboxService).publish(any(), any(), eq(HOSPITAL_ID));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private PrescriptionConfirmedEvent event(String eventId) {
        return new PrescriptionConfirmedEvent(eventId, PRESCRIPTION_ID, PATIENT_ID, HOSPITAL_ID,
                "corr-001", LocalDateTime.now());
    }

    private ConsumerRecord<String, String> record(PrescriptionConfirmedEvent event) throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        String json = mapper.writeValueAsString(event);
        return new ConsumerRecord<>("prescription-confirmed", 0, 0L, HOSPITAL_ID, json);
    }

    private com.careround.patient.entity.Patient patientWithWard(String wardId) {
        com.careround.patient.entity.Patient p = new com.careround.patient.entity.Patient();
        p.setId(PATIENT_ID);
        p.setHospitalId(HOSPITAL_ID);
        p.setWardId(wardId);
        p.setFirstName("Jane");
        p.setLastName("Doe");
        p.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        p.setAdmissionDate(LocalDateTime.now());
        p.setAdmissionType(com.careround.patient.enums.AdmissionType.EMERGENCY);
        p.setHospitalNumber("HN-001");
        return p;
    }
}
