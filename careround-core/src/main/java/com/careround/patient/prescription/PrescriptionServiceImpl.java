package com.careround.patient.prescription;

import com.careround.patient.prescription.dto.PrescriptionResponse;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.prescription.enums.PrescriptionStatus;
import com.careround.shared.event.PrescriptionDiscontinuedEvent;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getActivePrescriptions(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        return prescriptionRepository
                .findAllByPatientIdAndHospitalIdAndStatus(patientId, hospitalId, PrescriptionStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PrescriptionResponse discontinue(String prescriptionId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        Prescription prescription = prescriptionRepository.findByIdAndHospitalId(prescriptionId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + prescriptionId));

        if (prescription.getStatus() != PrescriptionStatus.ACTIVE) {
            throw new BusinessRuleException("Only ACTIVE prescriptions can be discontinued");
        }

        prescription.setStatus(PrescriptionStatus.DISCONTINUED);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        outboxService.publish("prescription-discontinued",
                new PrescriptionDiscontinuedEvent(UUID.randomUUID().toString(), prescriptionId,
                        prescription.getPatientId(), hospitalId, userId, now,
                        MDC.get("correlationId"), now),
                hospitalId);

        log.info("action=PRESCRIPTION_DISCONTINUED prescriptionId={} hospitalId={} userId={}",
                prescriptionId, hospitalId, userId);
        return toResponse(prescription);
    }

    private PrescriptionResponse toResponse(Prescription p) {
        return new PrescriptionResponse(
                p.getId(), p.getPatientId(), p.getHospitalId(), p.getClinicalNoteId(),
                p.getDrugName(), p.getDose(), p.getRoute(), p.getFrequencyString(),
                p.getFrequencyHours(), p.getTotalDoses(), p.getStartTime(),
                p.getAdministrationTimes(), p.getConfirmedById(), p.getConfirmedAt(),
                p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
