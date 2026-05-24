package com.careround.patient.patient;

import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientRequest;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.PatientAdmittedEvent;
import com.careround.shared.event.PatientDischargedEvent;
import com.careround.shared.event.PatientUpdatedEvent;
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
import java.util.UUID;

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

        String userId = HospitalContextHolder.getUserId();

        Patient patient = new Patient();
        patient.setHospitalId(hospitalId);
        patient.setWardId(request.wardId());
        patient.setBedNumber(request.bedNumber());
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setHospitalNumber(request.hospitalNumber());
        patient.setPhoneNumber(request.phoneNumber());
        patient.setAddress(request.address());
        patient.setPreviousConditions(request.previousConditions());
        patient.setCurrentMedications(request.currentMedications());
        patient.setAllergies(request.allergies());
        patient.setEmergencyContactName(request.emergencyContactName());
        patient.setEmergencyContactPhone(request.emergencyContactPhone());
        patient.setRegisteredById(userId);
        patient.setAdmissionType(request.admissionType());
        patient.setPrimaryDiagnosis(request.primaryDiagnosis());
        patient.setEstimatedDischargeDate(request.estimatedDischargeDate());
        patient.setStatus(PatientStatus.ADMITTED);
        patient.setAdmissionDate(LocalDateTime.now(ZoneOffset.UTC));

        Patient saved = patientRepository.save(patient);

        outboxService.publish("patient-admitted",
                new PatientAdmittedEvent(UUID.randomUUID().toString(), hospitalId, saved.getId(), saved.getWardId(), MDC.get("correlationId")),
                hospitalId);

        log.info("action=admitPatient patientId={} hospitalId={} wardId={}", saved.getId(), hospitalId, saved.getWardId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatient(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients(PatientStatus status, String nameQuery) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        List<Patient> patients;
        if (nameQuery != null && !nameQuery.isBlank()) {
            patients = patientRepository.searchByHospitalAndName(hospitalId, status, nameQuery.trim());
        } else if (status != null) {
            patients = patientRepository.findAllByHospitalIdAndStatusOrderByAdmissionDateDesc(hospitalId, status);
        } else {
            patients = patientRepository.findAllByHospitalIdOrderByAdmissionDateDesc(hospitalId);
        }
        return patients.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatients(String hospitalId, String wardId, PatientStatus status) {
        List<Patient> patients;
        if (wardId != null && !wardId.isBlank() && status != null) {
            patients = patientRepository.findAllByHospitalIdAndWardIdAndStatusOrderByAdmissionDateDesc(
                    hospitalId, wardId, status);
        } else if (wardId != null && !wardId.isBlank()) {
            patients = patientRepository.findAllByHospitalIdAndWardIdOrderByAdmissionDateDesc(hospitalId, wardId);
        } else if (status != null) {
            patients = patientRepository.findAllByHospitalIdAndStatusOrderByAdmissionDateDesc(hospitalId, status);
        } else {
            patients = patientRepository.findAllByHospitalIdOrderByAdmissionDateDesc(hospitalId);
        }
        return patients.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByWard(String wardId, String nameQuery) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        List<Patient> patients = (nameQuery != null && !nameQuery.isBlank())
                ? patientRepository.searchAdmittedByWardAndName(hospitalId, wardId, PatientStatus.ADMITTED, nameQuery.trim())
                : patientRepository.findAllByHospitalIdAndWardIdAndStatusOrderByAdmissionDateDesc(hospitalId, wardId, PatientStatus.ADMITTED);
        return patients.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(String patientId, UpdatePatientRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        if (request.wardId() != null) {
            Ward ward = wardRepository.findByIdAndHospitalId(request.wardId(), hospitalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
            if (!ward.getHospitalId().equals(hospitalId)) {
                throw new AccessDeniedException("Ward does not belong to this hospital");
            }
        }

        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setAdmissionType(request.admissionType());
        patient.setWardId(request.wardId());
        patient.setBedNumber(request.bedNumber());
        patient.setPhoneNumber(request.phoneNumber());
        patient.setAddress(request.address());
        patient.setPreviousConditions(request.previousConditions());
        patient.setCurrentMedications(request.currentMedications());
        patient.setAllergies(request.allergies());
        patient.setEmergencyContactName(request.emergencyContactName());
        patient.setEmergencyContactPhone(request.emergencyContactPhone());

        outboxService.publish("patient-updated",
                new PatientUpdatedEvent(UUID.randomUUID().toString(), patientId, hospitalId,
                        patient.getWardId(), patient.getStatus().name(), MDC.get("correlationId"),
                        LocalDateTime.now(ZoneOffset.UTC)),
                hospitalId);

        log.info("action=updatePatient patientId={} hospitalId={}", patientId, hospitalId);
        return toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse updatePatientStatus(String patientId, UpdatePatientStatusRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        PatientStatus current = patient.getStatus();
        PatientStatus target = request.status();

        if (current == target) {
            throw new BusinessRuleException("Patient is already " + target.name().toLowerCase());
        }

        if (target == PatientStatus.DISCHARGED) {
            String wardId = patient.getWardId();
            outboxService.publish("patient-discharged",
                    new PatientDischargedEvent(UUID.randomUUID().toString(), hospitalId, patientId, wardId,
                            LocalDateTime.now(ZoneOffset.UTC), MDC.get("correlationId")),
                    hospitalId);
            patient.setWardId(null);
            patient.setBedNumber(null);
        } else if (target == PatientStatus.ADMITTED) {
            patient.setAdmissionDate(LocalDateTime.now(ZoneOffset.UTC));
            outboxService.publish("patient-admitted",
                    new PatientAdmittedEvent(UUID.randomUUID().toString(), hospitalId, patientId,
                            patient.getWardId(), MDC.get("correlationId")),
                    hospitalId);
        }

        patient.setStatus(target);

        outboxService.publish("patient-updated",
                new PatientUpdatedEvent(UUID.randomUUID().toString(), patientId, hospitalId,
                        patient.getWardId(), target.name(), MDC.get("correlationId"),
                        LocalDateTime.now(ZoneOffset.UTC)),
                hospitalId);

        log.info("action=updatePatientStatus patientId={} hospitalId={} from={} to={}", patientId, hospitalId, current, target);
        return toResponse(patient);
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(
                p.getId(), p.getHospitalId(), p.getWardId(),
                p.getFirstName(), p.getLastName(), p.getHospitalNumber(),
                p.getDateOfBirth(), p.getGender(), p.getBedNumber(),
                p.getPhoneNumber(), p.getAddress(),
                p.getPreviousConditions(), p.getCurrentMedications(), p.getAllergies(),
                p.getEmergencyContactName(), p.getEmergencyContactPhone(),
                p.getRegisteredById(),
                p.getAdmissionType(), p.getPrimaryDiagnosis(),
                p.getAcuityColor(), p.getEstimatedDischargeDate(),
                p.getStatus(), p.getAdmissionDate(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
