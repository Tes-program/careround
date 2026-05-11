package com.careround.patient.patient;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.Department;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.OnCallRotation;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.repository.DepartmentRepository;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.OnCallRotationRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.enums.PatientStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.MarkDischargeReadyRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.PatientAdmittedEvent;
import com.careround.shared.event.PatientDischargedEvent;
import com.careround.shared.event.PatientDischargeReadyEvent;
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
    private final MedicalTeamRepository medicalTeamRepository;
    private final DepartmentRepository departmentRepository;
    private final OnCallRotationRepository onCallRotationRepository;
    private final CareTaskRepository careTaskRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public PatientResponse admitPatient(AdmitPatientRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Ward ward = wardRepository.findById(request.wardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        if (!ward.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Ward does not belong to this hospital");
        }

        MedicalTeam team = medicalTeamRepository.findById(request.medicalTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));
        if (!team.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Medical team does not belong to this hospital");
        }

        String consultantId = resolveConsultant(hospitalId, request);

        Patient patient = new Patient();
        patient.setHospitalId(hospitalId);
        patient.setWardId(request.wardId());
        patient.setMedicalTeamId(request.medicalTeamId());
        patient.setAdmittingConsultantId(consultantId);
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setHospitalNumber(request.hospitalNumber());
        patient.setAdmissionType(request.admissionType());
        patient.setPrimaryDiagnosis(request.primaryDiagnosis());
        patient.setSpecialtyRequired(request.specialtyRequired());
        patient.setEstimatedDischargeDate(request.estimatedDischargeDate());
        patient.setStatus(PatientStatus.ADMITTED);
        patient.setAdmissionDate(LocalDateTime.now(ZoneOffset.UTC));

        Patient saved = patientRepository.save(patient);

        PatientAdmittedEvent event = new PatientAdmittedEvent(
                hospitalId, saved.getId(), saved.getWardId(),
                saved.getMedicalTeamId(), saved.getAdmittingConsultantId(),
                MDC.get("correlationId"));
        outboxService.publish("careround.patient.admitted", event, hospitalId);

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
        return patientRepository.findAllByHospitalIdAndWardIdAndStatusOrderByNewsScoreDescAdmissionDateAsc(
                        hospitalId, wardId, PatientStatus.ADMITTED)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(String query) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        return patientRepository.searchByHospitalId(hospitalId, query)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PatientResponse markDischargeReady(String patientId, MarkDischargeReadyRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        if (!HospitalContextHolder.hasRole(UserRole.CONSULTANT)) {
            throw new AccessDeniedException("Only consultants can mark patients as discharge ready");
        }

        Patient patient = patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patient.setDischargeReady(true);
        if (request.estimatedDischargeDate() != null) {
            patient.setEstimatedDischargeDate(request.estimatedDischargeDate());
        }

        PatientDischargeReadyEvent event = new PatientDischargeReadyEvent(
                hospitalId, patientId, patient.getWardId(),
                patient.getMedicalTeamId(), patient.getEstimatedDischargeDate(),
                MDC.get("correlationId"));
        outboxService.publish("careround.patient.discharge-ready", event, hospitalId);

        log.info("action=markDischargeReady patientId={} hospitalId={}", patientId, hospitalId);
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

        if (current != PatientStatus.ADMITTED) {
            throw new BusinessRuleException("Status transition only allowed from ADMITTED");
        }
        if (target == PatientStatus.ADMITTED) {
            throw new BusinessRuleException("Cannot transition to ADMITTED from ADMITTED");
        }

        if (target == PatientStatus.DISCHARGED) {
            long incomplete = careTaskRepository.findAllByPatientId(patientId).stream()
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                    .count();
            if (incomplete > 0) {
                throw new BusinessRuleException("Cannot discharge patient: " + incomplete + " incomplete care task(s) remain");
            }

            LocalDateTime dischargedAt = LocalDateTime.now(ZoneOffset.UTC);
            outboxService.publish("careround.patient.discharged",
                    new PatientDischargedEvent(hospitalId, patientId, patient.getWardId(),
                            dischargedAt, MDC.get("correlationId")),
                    hospitalId);
        }

        patient.setStatus(target);
        log.info("action=updatePatientStatus patientId={} hospitalId={} status={}", patientId, hospitalId, target);
        return toResponse(patient);
    }

    private String resolveConsultant(String hospitalId, AdmitPatientRequest request) {
        if (request.admittingConsultantId() != null) {
            return request.admittingConsultantId();
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Department> departments = departmentRepository.findAllByHospitalId(hospitalId);

        String consultantId = findOnCallConsultant(departments, request.specialtyRequired(), hospitalId, now);
        if (consultantId != null) return consultantId;

        consultantId = findOnCallConsultant(departments, "General Medicine", hospitalId, now);
        if (consultantId != null) return consultantId;

        throw new BusinessRuleException("No on-call consultant found for specialty: " + request.specialtyRequired());
    }

    private String findOnCallConsultant(List<Department> departments, String specialty, String hospitalId, LocalDateTime now) {
        return departments.stream()
                .filter(d -> d.getName().equalsIgnoreCase(specialty))
                .findFirst()
                .flatMap(dept -> onCallRotationRepository
                        .findFirstByHospitalIdAndDepartmentIdAndRoleAndStartTimeBeforeAndEndTimeAfter(
                                hospitalId, dept.getId(), OnCallRole.CONSULTANT_ON_CALL, now, now))
                .map(OnCallRotation::getDoctorId)
                .orElse(null);
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(
                p.getId(), p.getWardId(), p.getMedicalTeamId(), p.getAdmittingConsultantId(),
                p.getFirstName(), p.getLastName(), p.getHospitalNumber(), p.getDateOfBirth(),
                p.getGender(), p.getBedNumber(), p.getAdmissionType(), p.getPrimaryDiagnosis(),
                p.getSpecialtyRequired(), p.getAcuityLevel(), p.getNewsScore(),
                p.isDischargeReady(), p.getEstimatedDischargeDate(), p.getStatus(),
                p.getAdmissionDate(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
