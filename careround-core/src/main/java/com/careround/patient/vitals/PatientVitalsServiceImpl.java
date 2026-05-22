package com.careround.patient.vitals;

import com.careround.patient.entity.Patient;
import com.careround.patient.entity.PatientVitals;
import com.careround.patient.enums.VhiStatus;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientVitalsRepository;
import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientVitalsServiceImpl implements PatientVitalsService {

    private final PatientVitalsRepository patientVitalsRepository;
    private final PatientRepository patientRepository;
    private final AcuityComputationService acuityComputationService;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public VitalsResponse recordVitals(String patientId, RecordVitalsRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        int score = acuityComputationService.computeScore(
                request.pulse(), request.systolicBp(),
                request.respiratoryRate(), request.temperature(), request.spo2());
        VhiStatus vhiStatus = acuityComputationService.computeVhiStatus(score);
        VhiStatus previousVhiStatus = deriveVhiStatus(patient);

        PatientVitals vitals = new PatientVitals();
        vitals.setPatientId(patientId);
        vitals.setHospitalId(hospitalId);
        vitals.setRecordedById(userId);
        vitals.setPulse(request.pulse());
        vitals.setSystolicBp(request.systolicBp());
        vitals.setDiastolicBp(request.diastolicBp());
        vitals.setRespiratoryRate(request.respiratoryRate());
        vitals.setTemperature(request.temperature());
        vitals.setSpo2(request.spo2());
        vitals.setVhiScore(score);
        vitals.setVhiStatus(vhiStatus);
        vitals.setRecordedAt(LocalDateTime.now(ZoneOffset.UTC));

        patient.setAcuityColor(acuityComputationService.toAcuityColor(vhiStatus));

        PatientVitals saved = patientVitalsRepository.save(vitals);

        outboxService.publish("vitals-recorded",
                new com.careround.shared.event.VitalsRecordedEvent(
                        UUID.randomUUID().toString(),
                        saved.getId(),
                        patientId,
                        hospitalId,
                        userId,
                        score,
                        vhiStatus.name(),
                        previousVhiStatus != null ? previousVhiStatus.name() : null,
                        saved.getRecordedAt(),
                        MDC.get("correlationId"),
                        saved.getRecordedAt()),
                hospitalId);

        log.info("action=recordVitals patientId={} hospitalId={} vhiScore={} vhiStatus={}",
                patientId, hospitalId, score, vhiStatus);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalsResponse> getVitalsHistory(String patientId, int limit) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        int effectiveLimit = (limit <= 0 ? 10 : Math.min(limit, 50));
        return patientVitalsRepository.findAllByPatientIdAndHospitalIdOrderByRecordedAtDesc(patientId, hospitalId)
                .stream()
                .limit(effectiveLimit)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VitalsResponse getLatestVitals(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return patientVitalsRepository.findFirstByPatientIdAndHospitalIdOrderByRecordedAtDesc(patientId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No vitals recorded for this patient"));
    }

    private VhiStatus deriveVhiStatus(Patient patient) {
        if (patient.getAcuityColor() == null) return null;
        return switch (patient.getAcuityColor()) {
            case RED   -> VhiStatus.CRITICAL;
            case AMBER -> VhiStatus.WATCH;
            case GREEN -> VhiStatus.STABLE;
        };
    }

    private VitalsResponse toResponse(PatientVitals v) {
        return new VitalsResponse(
                v.getId(), v.getPatientId(), v.getHospitalId(), v.getRecordedById(),
                v.getPulse(), v.getSystolicBp(), v.getDiastolicBp(),
                v.getRespiratoryRate(), v.getTemperature(), v.getSpo2(),
                v.getVhiScore(), v.getVhiStatus(), v.getRecordedAt());
    }
}
