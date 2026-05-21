package com.careround.kafka.consumer;

import com.careround.patient.enums.AdmissionType;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.medicationchart.MedicationChartRepository;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.MedicationChartCreatedEvent;
import com.careround.shared.event.OutboxEventRepository;
import com.careround.shared.event.PrescriptionConfirmedEvent;
import com.careround.shared.event.ProcessedEventRepository;
import com.careround.shared.service.OutboxService;
import com.careround.test.DataJpaH2Test;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaH2Test
@Import({PrescriptionConfirmedConsumer.class, MedicationChartCreatedConsumer.class})
class PrescriptionChainIntegrationTest {

    @TestConfiguration
    static class Cfg {
        @Bean
        ObjectMapper objectMapper() {
            return JsonMapper.builder().findAndAddModules().build();
        }

        @Bean
        OutboxService outboxService(OutboxEventRepository r, ObjectMapper m) {
            return new OutboxService(r, m);
        }
    }

    @Autowired private PrescriptionConfirmedConsumer prescriptionConfirmedConsumer;
    @Autowired private MedicationChartCreatedConsumer medicationChartCreatedConsumer;
    @Autowired private PatientRepository patientRepository;
    @Autowired private PrescriptionRepository prescriptionRepository;
    @Autowired private MedicationChartRepository medicationChartRepository;
    @Autowired private MedicationTaskRepository medicationTaskRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private ObjectMapper objectMapper;

    private static final String HOSPITAL_ID = "hosp-chain";
    private static final String WARD_ID = "ward-chain";
    private static final String PATIENT_ID = "patient-chain";
    private static final String PRESCRIPTION_ID = "rx-chain";

    @BeforeEach
    void setUp() {
        com.careround.patient.entity.Patient patient = new com.careround.patient.entity.Patient();
        patient.setId(PATIENT_ID);
        patient.setHospitalId(HOSPITAL_ID);
        patient.setWardId(WARD_ID);
        patient.setFirstName("John");
        patient.setLastName("Smith");
        patient.setDateOfBirth(LocalDate.of(1975, 6, 15));
        patient.setGender("M");
        patient.setHospitalNumber("HN-CHAIN");
        patient.setAdmissionDate(LocalDateTime.now());
        patient.setAdmissionType(AdmissionType.ELECTIVE);
        patient.setStatus(PatientStatus.ADMITTED);
        patientRepository.save(patient);

        Prescription prescription = new Prescription();
        prescription.setId(PRESCRIPTION_ID);
        prescription.setPatientId(PATIENT_ID);
        prescription.setHospitalId(HOSPITAL_ID);
        prescription.setDrugName("Aspirin");
        prescription.setDose("100mg");
        prescription.setRoute("oral");
        prescription.setFrequencyString("every 8 hours");
        prescription.setFrequencyHours(8);
        prescription.setTotalDoses(3);
        prescription.setStartTime(LocalDateTime.now());
        prescription.setAdministrationTimes(List.of(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(9),
                LocalDateTime.now().plusHours(17)));
        prescription.setConfirmedById("doctor-1");
        prescription.setConfirmedAt(LocalDateTime.now());
        prescriptionRepository.save(prescription);
    }

    @Test
    void prescriptionConfirmed_createsChart_andOutboxEvent() throws Exception {
        prescriptionConfirmedConsumer.consume(prescriptionConfirmedRecord("evt-1"));

        assertThat(medicationChartRepository.count()).isEqualTo(1);
        assertThat(outboxEventRepository.findAll())
                .anyMatch(e -> "medication-chart-created".equals(e.getEventType()));
    }

    @Test
    void medicationChartCreated_createsTasks_forEachAdministrationTime() throws Exception {
        prescriptionConfirmedConsumer.consume(prescriptionConfirmedRecord("evt-2"));

        String outboxPayload = outboxEventRepository.findAll().stream()
                .filter(e -> "medication-chart-created".equals(e.getEventType()))
                .findFirst().orElseThrow().getPayload();
        MedicationChartCreatedEvent chartEvent = objectMapper.readValue(outboxPayload, MedicationChartCreatedEvent.class);

        medicationChartCreatedConsumer.consume(chartCreatedRecord("evt-chart-2", chartEvent));

        assertThat(medicationTaskRepository.count()).isEqualTo(3);
    }

    @Test
    void prescriptionConfirmed_idempotent_sameEventTwice() throws Exception {
        prescriptionConfirmedConsumer.consume(prescriptionConfirmedRecord("evt-idem-1"));
        prescriptionConfirmedConsumer.consume(prescriptionConfirmedRecord("evt-idem-1"));

        assertThat(medicationChartRepository.count()).isEqualTo(1);
    }

    @Test
    void medicationChartCreated_idempotent_sameEventTwice() throws Exception {
        prescriptionConfirmedConsumer.consume(prescriptionConfirmedRecord("evt-3"));

        String outboxPayload = outboxEventRepository.findAll().stream()
                .filter(e -> "medication-chart-created".equals(e.getEventType()))
                .findFirst().orElseThrow().getPayload();
        MedicationChartCreatedEvent chartEvent = objectMapper.readValue(outboxPayload, MedicationChartCreatedEvent.class);

        medicationChartCreatedConsumer.consume(chartCreatedRecord("evt-chart-idem", chartEvent));
        medicationChartCreatedConsumer.consume(chartCreatedRecord("evt-chart-idem", chartEvent));

        assertThat(medicationTaskRepository.count()).isEqualTo(3);
    }

    @Test
    void fullChain_endToEnd() throws Exception {
        prescriptionConfirmedConsumer.consume(prescriptionConfirmedRecord("evt-full"));

        assertThat(medicationChartRepository.count()).isEqualTo(1);
        assertThat(outboxEventRepository.findAll())
                .anyMatch(e -> "medication-chart-created".equals(e.getEventType()));

        String outboxPayload = outboxEventRepository.findAll().stream()
                .filter(e -> "medication-chart-created".equals(e.getEventType()))
                .findFirst().orElseThrow().getPayload();
        MedicationChartCreatedEvent chartEvent = objectMapper.readValue(outboxPayload, MedicationChartCreatedEvent.class);

        assertThat(chartEvent.wardId()).isEqualTo(WARD_ID);
        assertThat(chartEvent.prescriptionId()).isEqualTo(PRESCRIPTION_ID);

        medicationChartCreatedConsumer.consume(chartCreatedRecord("evt-chart-full", chartEvent));

        assertThat(medicationTaskRepository.count()).isEqualTo(3);
        assertThat(processedEventRepository.count()).isEqualTo(2);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private ConsumerRecord<String, String> prescriptionConfirmedRecord(String eventId) throws Exception {
        PrescriptionConfirmedEvent event = new PrescriptionConfirmedEvent(
                eventId, PRESCRIPTION_ID, PATIENT_ID, HOSPITAL_ID,
                UUID.randomUUID().toString(), LocalDateTime.now());
        return new ConsumerRecord<>("prescription-confirmed", 0, 0L,
                HOSPITAL_ID, objectMapper.writeValueAsString(event));
    }

    private ConsumerRecord<String, String> chartCreatedRecord(String eventId, MedicationChartCreatedEvent original)
            throws Exception {
        MedicationChartCreatedEvent event = new MedicationChartCreatedEvent(
                eventId, original.medicationChartId(), original.prescriptionId(),
                original.patientId(), original.wardId(), original.hospitalId(),
                UUID.randomUUID().toString(), LocalDateTime.now());
        return new ConsumerRecord<>("medication-chart-created", 0, 0L,
                HOSPITAL_ID, objectMapper.writeValueAsString(event));
    }
}
