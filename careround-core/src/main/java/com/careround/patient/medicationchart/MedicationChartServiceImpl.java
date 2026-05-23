package com.careround.patient.medicationchart;

import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.patient.medicationchart.dto.AddManualMedicationRequest;
import com.careround.patient.medicationchart.dto.MedicationChartResponse;
import com.careround.patient.medicationchart.dto.UpdateMedicationChartRequest;
import com.careround.patient.medicationchart.entity.MedicationChart;
import com.careround.patient.medicationchart.enums.MedicationChartStatus;
import com.careround.patient.medicationtask.MedicationTaskRepository;
import com.careround.patient.medicationtask.entity.MedicationTask;
import com.careround.patient.medicationtask.enums.MedicationTaskStatus;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.prescription.enums.PrescriptionStatus;
import com.careround.patient.entity.Patient;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.ManualMedicationAddedEvent;
import com.careround.shared.exception.BusinessRuleException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationChartServiceImpl implements MedicationChartService {

    private final MedicationChartRepository medicationChartRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicationTaskRepository medicationTaskRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional(readOnly = true)
    public List<MedicationChartResponse> getChartForPatient(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return medicationChartRepository.findAllByPatientIdAndHospitalId(patientId, hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public MedicationChartResponse updateNurseNotes(String chartId, UpdateMedicationChartRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        MedicationChart chart = findChart(chartId, hospitalId);
        chart.setNurseNotes(request.nurseNotes());
        return toResponse(medicationChartRepository.save(chart));
    }

    @Override
    @Transactional
    public MedicationChartResponse addManualMedication(String patientId, AddManualMedicationRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        Prescription prescription = new Prescription();
        prescription.setPatientId(patientId);
        prescription.setHospitalId(hospitalId);
        prescription.setDrugName(request.drugName());
        prescription.setDose(request.dose());
        prescription.setRoute(request.route());
        prescription.setFrequencyString(request.frequencyString());
        prescription.setFrequencyHours(request.frequencyHours());
        prescription.setTotalDoses(request.totalDoses());
        prescription.setStartTime(request.startTime());
        prescription.setAdministrationTimes(request.administrationTimes());
        prescription.setConfirmedById(userId);
        prescription.setConfirmedAt(now);
        prescription.setStatus(PrescriptionStatus.ACTIVE);
        Prescription savedPrescription = prescriptionRepository.save(prescription);

        MedicationChart chart = new MedicationChart();
        chart.setPatientId(patientId);
        chart.setHospitalId(hospitalId);
        chart.setPrescriptionId(savedPrescription.getId());
        chart.setStatus(MedicationChartStatus.ACTIVE);
        MedicationChart savedChart = medicationChartRepository.save(chart);

        Map<String, Long> nursePendingCounts = loadNursePendingCounts(hospitalId, patient.getWardId());

        for (LocalDateTime time : request.administrationTimes()) {
            MedicationTask task = new MedicationTask();
            task.setMedicationChartId(savedChart.getId());
            task.setPatientId(patientId);
            task.setHospitalId(hospitalId);
            task.setWardId(patient.getWardId());
            task.setScheduledTime(time);
            task.setStatus(MedicationTaskStatus.PENDING);
            String nurseId = pickLeastLoadedNurse(nursePendingCounts);
            task.setAssignedNurseId(nurseId);
            if (nurseId != null) nursePendingCounts.merge(nurseId, 1L, Long::sum);
            medicationTaskRepository.save(task);
        }

        outboxService.publish("manual-medication-added",
                new ManualMedicationAddedEvent(UUID.randomUUID().toString(),
                        savedPrescription.getId(), patientId,
                        hospitalId, MDC.get("correlationId"), now),
                hospitalId);

        log.info("action=MANUAL_MEDICATION_ADDED patientId={} hospitalId={} drug={}",
                patientId, hospitalId, request.drugName());
        return toResponse(savedChart);
    }

    @Override
    @Transactional
    public MedicationChartResponse discontinue(String chartId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        MedicationChart chart = findChart(chartId, hospitalId);

        if (chart.getStatus() == MedicationChartStatus.DISCONTINUED) {
            throw new BusinessRuleException("Chart entry is already discontinued");
        }

        chart.setStatus(MedicationChartStatus.DISCONTINUED);

        medicationTaskRepository.findAllByMedicationChartId(chartId).stream()
                .filter(t -> t.getStatus() == MedicationTaskStatus.PENDING)
                .forEach(t -> {
                    t.setStatus(MedicationTaskStatus.OVERDUE);
                    medicationTaskRepository.save(t);
                });

        log.info("action=CHART_DISCONTINUED chartId={} hospitalId={}", chartId, hospitalId);
        return toResponse(medicationChartRepository.save(chart));
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

    private MedicationChart findChart(String chartId, String hospitalId) {
        return medicationChartRepository.findByIdAndHospitalId(chartId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication chart entry not found: " + chartId));
    }

    private MedicationChartResponse toResponse(MedicationChart c) {
        return new MedicationChartResponse(
                c.getId(), c.getPatientId(), c.getHospitalId(),
                c.getPrescriptionId(), c.getStatus(), c.getNurseNotes(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
