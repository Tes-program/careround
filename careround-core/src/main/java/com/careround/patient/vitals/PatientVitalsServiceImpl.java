package com.careround.patient.vitals;

import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.AcuityColor;
import com.careround.patient.enums.ConsciousnessLevel;
import com.careround.patient.enums.NoteType;
import com.careround.patient.repository.ClinicalNoteRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientVitalsServiceImpl implements PatientVitalsService {

    private final PatientVitalsRepository patientVitalsRepository;
    private final PatientRepository patientRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;

    @Override
    @Transactional
    public VitalsResponse recordVitals(String patientId, RecordVitalsRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = loadValidatedPatient(patientId, hospitalId);

        PatientVitals vitals = new PatientVitals();
        vitals.setPatientId(patientId);
        vitals.setHospitalId(hospitalId);
        vitals.setRecordedById(HospitalContextHolder.getUserId());
        vitals.setHeartRate(request.heartRate());
        vitals.setRespiratoryRate(request.respiratoryRate());
        vitals.setOxygenSaturation(request.oxygenSaturation());
        vitals.setSystolicBP(request.systolicBP());
        vitals.setTemperature(request.temperature());
        vitals.setConsciousnessLevel(request.consciousnessLevel());
        vitals.setRecordedAt(LocalDateTime.now(ZoneOffset.UTC));

        int score = computeNews2Score(request);
        vitals.setComputedScore(score);
        AcuityColor color = scoreToColor(score);
        vitals.setAcuityColor(color);

        patient.setAcuityColor(color);

        PatientVitals saved = patientVitalsRepository.save(vitals);
        createVitalsNoteIfPresent(patientId, hospitalId, request.note());
        log.info("action=recordVitals patientId={} hospitalId={} computedScore={}", patientId, hospitalId, score);
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

    private int computeNews2Score(RecordVitalsRequest r) {
        int score = 0;

        // Respiratory rate
        if (r.respiratoryRate() != null) {
            int rr = r.respiratoryRate();
            if (rr <= 8) score += 3;
            else if (rr <= 11) score += 1;
            else if (rr <= 20) score += 0;
            else if (rr <= 24) score += 2;
            else score += 3;
        }

        // Oxygen saturation (SpO2 ≥96 = 0, 94-95 = 1, 92-93 = 2, ≤91 = 3)
        if (r.oxygenSaturation() != null) {
            int spo2 = r.oxygenSaturation().intValue();
            if (spo2 >= 96) score += 0;
            else if (spo2 >= 94) score += 1;
            else if (spo2 >= 92) score += 2;
            else score += 3;
        }

        // Systolic BP
        if (r.systolicBP() != null) {
            int sbp = r.systolicBP();
            if (sbp <= 90) score += 3;
            else if (sbp <= 100) score += 2;
            else if (sbp <= 110) score += 1;
            else if (sbp <= 219) score += 0;
            else score += 3;
        }

        // Heart rate
        if (r.heartRate() != null) {
            int hr = r.heartRate();
            if (hr <= 40) score += 3;
            else if (hr <= 50) score += 1;
            else if (hr <= 90) score += 0;
            else if (hr <= 110) score += 1;
            else if (hr <= 130) score += 2;
            else score += 3;
        }

        // Temperature
        if (r.temperature() != null) {
            double temp = r.temperature().doubleValue();
            if (temp <= 35.0) score += 3;
            else if (temp <= 36.0) score += 1;
            else if (temp <= 38.0) score += 0;
            else if (temp <= 39.0) score += 1;
            else score += 2;
        }

        // Consciousness (AVPU: ALERT=0, any other=3)
        if (r.consciousnessLevel() != null && r.consciousnessLevel() != ConsciousnessLevel.ALERT) {
            score += 3;
        }

        return score;
    }

    private AcuityColor scoreToColor(int score) {
        if (score >= 7) return AcuityColor.RED;
        if (score >= 5) return AcuityColor.AMBER;
        return AcuityColor.GREEN;
    }

    private VitalsResponse toResponse(PatientVitals v) {
        return new VitalsResponse(
                v.getId(), v.getPatientId(), v.getHospitalId(), v.getRecordedById(),
                v.getHeartRate(), v.getRespiratoryRate(), v.getSystolicBP(),
                v.getOxygenSaturation(), v.getTemperature(), v.getConsciousnessLevel(),
                v.getComputedScore(), v.getAcuityColor(), v.getRecordedAt());
    }

    private void createVitalsNoteIfPresent(String patientId, String hospitalId, String noteText) {
        if (noteText == null || noteText.isBlank()) {
            return;
        }
        ClinicalNote note = new ClinicalNote();
        note.setPatientId(patientId);
        note.setHospitalId(hospitalId);
        note.setAuthorId(HospitalContextHolder.getUserId());
        note.setNoteType(NoteType.PROGRESS_NOTE);
        note.setContent(noteText.trim());
        clinicalNoteRepository.save(note);
    }
}
