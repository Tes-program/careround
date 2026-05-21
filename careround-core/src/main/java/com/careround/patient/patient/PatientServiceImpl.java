package com.careround.patient.patient;

import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.PatientAdmittedEvent;
import com.careround.shared.event.PatientDischargedEvent;
import com.careround.shared.exception.AccessDeniedException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final WardRepository wardRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public PatientResponse admitPatient(AdmitPatientRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        if (request.wardId() != null) {
            Ward ward = wardRepository.findById(request.wardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
            if (!ward.getHospitalId().equals(hospitalId)) {
                throw new AccessDeniedException("Ward does not belong to this hospital");
            }
        }

        Patient patient = new Patient();
        patient.setHospitalId(hospitalId);
        patient.setWardId(request.wardId());
        patient.setBedNumber(request.bedNumber());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setHospitalNumber(request.hospitalNumber());
        patient.setAdmissionType(request.admissionType());
        patient.setPrimaryDiagnosis(request.primaryDiagnosis());
        patient.setEstimatedDischargeDate(request.estimatedDischargeDate());
        patient.setStatus(PatientStatus.ADMITTED);
        patient.setAdmissionDate(LocalDateTime.now(ZoneOffset.UTC));

        Patient saved = patientRepository.save(patient);

        outboxService.publish("patient-admitted",
                new PatientAdmittedEvent(hospitalId, saved.getId(), saved.getWardId(), MDC.get("correlationId")),
                hospitalId);

        log.info("action=admitPatient patientId={} hospitalId={} wardId={}", saved.getId(), hospitalId, saved.getWardId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatient(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Access denied: patient belongs to another hospital");
        }
        return toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByWard(String wardId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        return patientRepository
                .findAllByHospitalIdAndWardIdAndStatusOrderByAdmissionDateAsc(
                        hospitalId, wardId, PatientStatus.ADMITTED)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PatientResponse updatePatientStatus(String patientId, UpdatePatientStatusRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        PatientStatus current = patient.getStatus();
        PatientStatus target = request.status();

        if (target == PatientStatus.ADMITTED) {
            throw new BusinessRuleException("Cannot transition to ADMITTED");
        }
        if (current == PatientStatus.DISCHARGED) {
            throw new BusinessRuleException("Patient is already discharged");
        }

        if (target == PatientStatus.DISCHARGED) {
            String wardId = patient.getWardId();
            outboxService.publish("patient-discharged",
                    new PatientDischargedEvent(hospitalId, patientId, wardId,
                            LocalDateTime.now(ZoneOffset.UTC), MDC.get("correlationId")),
                    hospitalId);
            patient.setWardId(null);
            patient.setBedNumber(null);
        }

        patient.setStatus(target);
        log.info("action=updatePatientStatus patientId={} hospitalId={} status={}", patientId, hospitalId, target);
        return toResponse(patient);
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(
                p.getId(), p.getHospitalId(), p.getWardId(),
                p.getFirstName(), p.getLastName(), p.getHospitalNumber(),
                p.getDateOfBirth(), p.getGender(), p.getBedNumber(),
                p.getAdmissionType(), p.getPrimaryDiagnosis(),
                p.getAcuityColor(), p.getEstimatedDischargeDate(),
                p.getStatus(), p.getAdmissionDate(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
