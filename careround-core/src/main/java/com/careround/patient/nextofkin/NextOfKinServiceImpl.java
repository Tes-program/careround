package com.careround.patient.nextofkin;

import com.careround.patient.entity.NextOfKin;
import com.careround.patient.entity.Patient;
import com.careround.patient.nextofkin.dto.AddNextOfKinRequest;
import com.careround.patient.nextofkin.dto.NextOfKinResponse;
import com.careround.patient.nextofkin.dto.UpdateNextOfKinRequest;
import com.careround.patient.nextofkin.dto.UpdateNotificationConsentRequest;
import com.careround.patient.repository.NextOfKinRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NextOfKinServiceImpl implements NextOfKinService {

    private final NextOfKinRepository nextOfKinRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public NextOfKinResponse addNextOfKin(String patientId, AddNextOfKinRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        loadValidatedPatient(patientId, hospitalId);

        if (request.isEmergencyContact()) {
            clearExistingEmergencyContacts(patientId);
        }

        NextOfKin nok = new NextOfKin();
        nok.setPatientId(patientId);
        nok.setName(request.name());
        nok.setRelationship(request.relationship());
        nok.setPhone(request.phone());
        nok.setEmail(request.email());
        nok.setPreferredContactMethod(request.preferredContactMethod());
        nok.setEmergencyContact(request.isEmergencyContact());
        nok.setNotificationConsent(request.notificationConsent());

        NextOfKin saved = nextOfKinRepository.save(nok);
        log.info("action=addNextOfKin patientId={} hospitalId={} nokId={}", patientId, hospitalId, saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public NextOfKinResponse updateNextOfKin(String patientId, String nokId, UpdateNextOfKinRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        loadValidatedPatient(patientId, hospitalId);

        NextOfKin nok = nextOfKinRepository.findByIdAndPatientId(nokId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Next of kin not found"));

        if (Boolean.TRUE.equals(request.isEmergencyContact())) {
            clearExistingEmergencyContacts(patientId);
            nok.setEmergencyContact(true);
        } else if (Boolean.FALSE.equals(request.isEmergencyContact())) {
            nok.setEmergencyContact(false);
        }

        if (request.name() != null) nok.setName(request.name());
        if (request.relationship() != null) nok.setRelationship(request.relationship());
        if (request.phone() != null) nok.setPhone(request.phone());
        if (request.email() != null) nok.setEmail(request.email());
        if (request.preferredContactMethod() != null) nok.setPreferredContactMethod(request.preferredContactMethod());
        if (request.notificationConsent() != null) nok.setNotificationConsent(request.notificationConsent());

        log.info("action=updateNextOfKin patientId={} hospitalId={} nokId={}", patientId, hospitalId, nokId);
        return toResponse(nok);
    }

    @Override
    @Transactional
    public void removeNextOfKin(String patientId, String nokId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        loadValidatedPatient(patientId, hospitalId);

        NextOfKin nok = nextOfKinRepository.findByIdAndPatientId(nokId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Next of kin not found"));

        if (nok.isEmergencyContact()) {
            log.warn("action=removeEmergencyContact patientId={} nokId={} - removing the only emergency contact", patientId, nokId);
        }

        nextOfKinRepository.delete(nok);
        log.info("action=removeNextOfKin patientId={} hospitalId={} nokId={}", patientId, hospitalId, nokId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NextOfKinResponse> getNextOfKin(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        loadValidatedPatient(patientId, hospitalId);
        return nextOfKinRepository.findAllByPatientId(patientId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public NextOfKinResponse updateNotificationConsent(String patientId, String nokId,
            UpdateNotificationConsentRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        loadValidatedPatient(patientId, hospitalId);

        NextOfKin nok = nextOfKinRepository.findByIdAndPatientId(nokId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Next of kin not found"));

        nok.setNotificationConsent(request.consent());
        log.info("action=updateNotificationConsent patientId={} nokId={} consent={}", patientId, nokId, request.consent());
        return toResponse(nok);
    }

    private void clearExistingEmergencyContacts(String patientId) {
        nextOfKinRepository.findEmergencyContactsByPatientId(patientId)
                .forEach(n -> n.setEmergencyContact(false));
    }

    private Patient loadValidatedPatient(String patientId, String hospitalId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Access denied: patient belongs to another hospital");
        }
        return patient;
    }

    private NextOfKinResponse toResponse(NextOfKin n) {
        return new NextOfKinResponse(
                n.getId(), n.getPatientId(), n.getName(), n.getRelationship(),
                n.getPhone(), n.getEmail(), n.getPreferredContactMethod(),
                n.isEmergencyContact(), n.isNotificationConsent(),
                n.getCreatedAt(), n.getUpdatedAt());
    }
}
