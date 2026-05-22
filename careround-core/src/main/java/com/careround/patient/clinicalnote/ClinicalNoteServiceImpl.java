package com.careround.patient.clinicalnote;

import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.ConfirmNoteRequest;
import com.careround.patient.clinicalnote.dto.ConfirmNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.prescription.PrescriptionRepository;
import com.careround.patient.prescription.dto.CreatePrescriptionRequest;
import com.careround.patient.prescription.entity.Prescription;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.ClinicalNoteSavedEvent;
import com.careround.shared.event.PrescriptionConfirmedEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicalNoteServiceImpl implements ClinicalNoteService {

    private final ClinicalNoteRepository clinicalNoteRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public ClinicalNoteResponse createNote(CreateClinicalNoteRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        patientRepository.findByIdAndHospitalId(request.patientId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        ClinicalNote note = new ClinicalNote();
        note.setPatientId(request.patientId());
        note.setHospitalId(hospitalId);
        note.setAuthorId(userId);
        note.setNoteType(request.noteType());
        note.setContent(request.content());
        note.setRawTranscription(request.rawTranscription());
        note.setAiGenerated(request.isAiGenerated());
        note.setAiModelUsed(request.aiModelUsed());

        ClinicalNote saved = clinicalNoteRepository.save(note);
        log.info("action=createNote noteId={} patientId={} authorId={}", saved.getId(), request.patientId(), userId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> getPatientNotes(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return clinicalNoteRepository.findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc(patientId, hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ConfirmNoteResponse confirm(ConfirmNoteRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        patientRepository.findByIdAndHospitalId(request.patientId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        ClinicalNote note = new ClinicalNote();
        note.setPatientId(request.patientId());
        note.setHospitalId(hospitalId);
        note.setAuthorId(userId);
        note.setNoteType(request.noteType());
        note.setContent(request.content());
        note.setRawTranscription(request.rawTranscription());
        note.setAiGenerated(request.isAiGenerated());
        note.setAiModelUsed(request.aiModelUsed());
        if (request.isAiGenerated()) {
            note.setConfirmedByDoctorAt(LocalDateTime.now(ZoneOffset.UTC));
        }
        ClinicalNote savedNote = clinicalNoteRepository.save(note);

        List<String> prescriptionIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        for (CreatePrescriptionRequest pr : request.prescriptions()) {
            Prescription prescription = new Prescription();
            prescription.setPatientId(request.patientId());
            prescription.setHospitalId(hospitalId);
            prescription.setClinicalNoteId(savedNote.getId());
            prescription.setDrugName(pr.drugName());
            prescription.setDose(pr.dose());
            prescription.setRoute(pr.route());
            prescription.setFrequencyString(pr.frequencyString());
            prescription.setFrequencyHours(pr.frequencyHours());
            prescription.setTotalDoses(pr.totalDoses());
            prescription.setStartTime(pr.startTime());
            prescription.setAdministrationTimes(pr.administrationTimes());
            prescription.setConfirmedById(userId);
            prescription.setConfirmedAt(now);
            Prescription savedPrescription = prescriptionRepository.save(prescription);
            prescriptionIds.add(savedPrescription.getId());

            outboxService.publish("prescription-confirmed",
                    new PrescriptionConfirmedEvent(UUID.randomUUID().toString(),
                            savedPrescription.getId(), request.patientId(),
                            hospitalId, MDC.get("correlationId"), now),
                    hospitalId);
        }

        outboxService.publish("clinical-note-saved",
                new ClinicalNoteSavedEvent(UUID.randomUUID().toString(),
                        savedNote.getId(), request.patientId(),
                        hospitalId, MDC.get("correlationId"), now),
                hospitalId);

        log.info("action=confirmNote noteId={} prescriptions={} patientId={}",
                savedNote.getId(), prescriptionIds.size(), request.patientId());
        return new ConfirmNoteResponse(savedNote.getId(), prescriptionIds);
    }

    private ClinicalNoteResponse toResponse(ClinicalNote n) {
        return new ClinicalNoteResponse(
                n.getId(), n.getPatientId(), n.getHospitalId(), n.getAuthorId(),
                n.getNoteType(), n.getContent(), n.getRawTranscription(),
                n.isAiGenerated(), n.getAiModelUsed(), n.getConfirmedByDoctorAt(),
                n.getCreatedAt(), n.getUpdatedAt());
    }
}
