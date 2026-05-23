package com.careround.kafka.consumer;

import com.careround.auth.repository.UserRepository;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.shared.event.MedicationChartCreatedEvent;
import com.careround.shared.event.ProcessedEvent;
import com.careround.shared.event.ProcessedEventRepository;
import com.careround.shared.security.HospitalContextHolder;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationChartCreatedConsumerTest {

    @Mock private ProcessedEventRepository processedEventRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private MedicationTaskRepository medicationTaskRepository;
    @Mock private UserRepository userRepository;
    @Spy private ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @InjectMocks private MedicationChartCreatedConsumer consumer;

    private static final String EVENT_ID = "evt-002";
    private static final String CHART_ID = "chart-001";
    private static final String PRESCRIPTION_ID = "rx-001";
    private static final String PATIENT_ID = "patient-001";
    private static final String HOSPITAL_ID = "hosp-001";
    private static final String WARD_ID = "ward-001";

    @Test
    void consume_createsTasks_forEachAdministrationTime() throws Exception {
        LocalDateTime t1 = LocalDateTime.now().plusHours(1);
        LocalDateTime t2 = LocalDateTime.now().plusHours(7);
        LocalDateTime t3 = LocalDateTime.now().plusHours(13);

        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(prescription(List.of(t1, t2, t3))));
        when(medicationTaskRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(record(event(EVENT_ID)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MedicationTask>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicationTaskRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
    }

    @Test
    void consume_skipsProcessing_whenEventAlreadyProcessed() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(true);

        consumer.consume(record(event(EVENT_ID)));

        verify(medicationTaskRepository, never()).saveAll(any());
    }

    @Test
    void consume_throwsException_whenPrescriptionNotFound() {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.consume(record(event(EVENT_ID))))
                .isInstanceOf(RuntimeException.class);

        verify(medicationTaskRepository, never()).saveAll(any());
    }

    @Test
    void consume_setsCorrectFields_wardIdFromEvent() throws Exception {
        LocalDateTime t1 = LocalDateTime.now().plusHours(1);
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(prescription(List.of(t1))));
        when(medicationTaskRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(record(event(EVENT_ID)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MedicationTask>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicationTaskRepository).saveAll(captor.capture());
        MedicationTask task = captor.getValue().getFirst();
        assertThat(task.getWardId()).isEqualTo(WARD_ID);
        assertThat(task.getPatientId()).isEqualTo(PATIENT_ID);
        assertThat(task.getHospitalId()).isEqualTo(HOSPITAL_ID);
        assertThat(task.getMedicationChartId()).isEqualTo(CHART_ID);
        assertThat(task.getScheduledTime()).isEqualTo(t1);
    }

    @Test
    void consume_setsStatus_PENDING_onAllTasks() throws Exception {
        LocalDateTime t1 = LocalDateTime.now().plusHours(1);
        LocalDateTime t2 = LocalDateTime.now().plusHours(7);
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(prescription(List.of(t1, t2))));
        when(medicationTaskRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(record(event(EVENT_ID)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MedicationTask>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicationTaskRepository).saveAll(captor.capture());
        captor.getValue().forEach(t -> assertThat(t.getStatus()).isEqualTo(MedicationTaskStatus.PENDING));
    }

    @Test
    void consume_usesHospitalIdFromEvent_notContextHolder() throws Exception {
        LocalDateTime t1 = LocalDateTime.now().plusHours(1);
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(prescription(List.of(t1))));
        when(medicationTaskRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        HospitalContextHolder.clear();
        consumer.consume(record(event(EVENT_ID)));

        verify(prescriptionRepository).findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID);
    }

    @Test
    void consume_usesSaveAll_notIndividualSaves() throws Exception {
        LocalDateTime t1 = LocalDateTime.now().plusHours(1);
        LocalDateTime t2 = LocalDateTime.now().plusHours(7);
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(prescription(List.of(t1, t2))));
        when(medicationTaskRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(record(event(EVENT_ID)));

        verify(medicationTaskRepository).saveAll(anyList());
        verify(medicationTaskRepository, never()).save(any());
    }

    @Test
    void consume_savesProcessedEvent() throws Exception {
        LocalDateTime t1 = LocalDateTime.now().plusHours(1);
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(prescription(List.of(t1))));
        when(medicationTaskRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(record(event(EVENT_ID)));

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(EVENT_ID);
    }

    @Test
    void consume_worksWithEmptyAdministrationTimes() throws Exception {
        when(processedEventRepository.existsById(EVENT_ID)).thenReturn(false);
        when(prescriptionRepository.findByIdAndHospitalId(PRESCRIPTION_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(prescription(List.of())));
        when(medicationTaskRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(record(event(EVENT_ID)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MedicationTask>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicationTaskRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).isEmpty();
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private MedicationChartCreatedEvent event(String eventId) {
        return new MedicationChartCreatedEvent(eventId, CHART_ID, PRESCRIPTION_ID,
                PATIENT_ID, WARD_ID, HOSPITAL_ID, "corr-001", LocalDateTime.now());
    }

    private ConsumerRecord<String, String> record(MedicationChartCreatedEvent event) throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        String json = mapper.writeValueAsString(event);
        return new ConsumerRecord<>("medication-chart-created", 0, 0L, HOSPITAL_ID, json);
    }

    private Prescription prescription(List<LocalDateTime> times) {
        Prescription p = new Prescription();
        p.setId(PRESCRIPTION_ID);
        p.setPatientId(PATIENT_ID);
        p.setHospitalId(HOSPITAL_ID);
        p.setDrugName("Aspirin");
        p.setDose("100mg");
        p.setRoute("oral");
        p.setFrequencyString("daily");
        p.setFrequencyHours(24);
        p.setTotalDoses(7);
        p.setStartTime(LocalDateTime.now());
        p.setAdministrationTimes(times);
        p.setConfirmedById("user-001");
        p.setConfirmedAt(LocalDateTime.now());
        return p;
    }
}
