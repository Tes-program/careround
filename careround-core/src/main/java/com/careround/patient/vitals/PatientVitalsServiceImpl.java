package com.careround.patient.vitals;

import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientVitalsRepository;
import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientVitalsServiceImpl implements PatientVitalsService {

    private final PatientVitalsRepository patientVitalsRepository;
    private final PatientRepository patientRepository;
    private final NewsScoreService newsScoreService;

    @Override
    @Transactional
    public VitalsResponse recordVitals(String patientId, RecordVitalsRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = loadValidatedPatient(patientId, hospitalId);

        PatientVitals vitals = new PatientVitals();
        vitals.setPatientId(patientId);
        vitals.setRecordedById(HospitalContextHolder.getUserId());
        vitals.setHeartRate(request.heartRate());
        vitals.setRespiratoryRate(request.respiratoryRate());
        vitals.setOxygenSaturation(request.oxygenSaturation());
        vitals.setSystolicBP(request.systolicBP());
        vitals.setTemperature(request.temperature());
        vitals.setConsciousnessLevel(request.consciousnessLevel());
        vitals.setRecordedAt(LocalDateTime.now(ZoneOffset.UTC));

        newsScoreService.computeAndUpdate(patient, vitals);

        PatientVitals saved = patientVitalsRepository.save(vitals);
        log.info("action=recordVitals patientId={} hospitalId={} newsScore={}", patientId, hospitalId, saved.getNewsScore());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalsResponse> getVitalsHistory(String patientId, int limit) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        loadValidatedPatient(patientId, hospitalId);

        int effectiveLimit = (limit <= 0 ? 10 : Math.min(limit, 50));
        return patientVitalsRepository.findAllByPatientIdOrderByRecordedAtDesc(patientId)
                .stream()
                .limit(effectiveLimit)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VitalsResponse getLatestVitals(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        loadValidatedPatient(patientId, hospitalId);
        return patientVitalsRepository.findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No vitals recorded for this patient"));
    }

    private Patient loadValidatedPatient(String patientId, String hospitalId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Access denied: patient belongs to another hospital");
        }
        return patient;
    }

    private VitalsResponse toResponse(PatientVitals v) {
        return new VitalsResponse(
                v.getId(), v.getPatientId(), v.getRecordedById(),
                v.getHeartRate(), v.getRespiratoryRate(), v.getSystolicBP(),
                v.getOxygenSaturation(), v.getTemperature(), v.getConsciousnessLevel(),
                v.getNewsScore(), v.getRecordedAt());
    }
}
