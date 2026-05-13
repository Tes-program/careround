package com.careround.patient.clinicalnote;

import com.careround.patient.clinicalnote.dto.AmendNoteRequest;
import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.PatientVitalsRepository;
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
public class ClinicalNoteServiceImpl implements ClinicalNoteService {

    private final ClinicalNoteRepository clinicalNoteRepository;
    private final PatientRepository patientRepository;
    private final PatientVitalsRepository patientVitalsRepository;

    @Override
    @Transactional
    public ClinicalNoteResponse createNote(CreateClinicalNoteRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        patientRepository.findByIdAndHospitalId(request.patientId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        validateVitalsLink(request.patientId(), request.vitalsId());

        ClinicalNote note = new ClinicalNote();
        note.setPatientId(request.patientId());
        note.setPatientRoundReviewId(request.patientRoundReviewId());
        note.setVitalsId(request.vitalsId());
        note.setAuthorId(userId);
        note.setNoteType(request.noteType());
        note.setContent(request.content());

        ClinicalNote saved = clinicalNoteRepository.save(note);
        log.info("action=createNote noteId={} patientId={} authorId={}", saved.getId(), request.patientId(), userId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ClinicalNoteResponse amendNote(String noteId, AmendNoteRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        ClinicalNote note = clinicalNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinical note not found"));

        patientRepository.findByIdAndHospitalId(note.getPatientId(), hospitalId)
                .orElseThrow(() -> new AccessDeniedException("Note does not belong to this hospital"));

        if (!note.getAuthorId().equals(userId))
            throw new AccessDeniedException("Only the note author can amend this note");

        note.setContent(request.content());
        note.setAmended(true);
        note.setAmendedById(userId);
        note.setAmendedAt(LocalDateTime.now(ZoneOffset.UTC));

        log.info("action=amendNote noteId={} amendedBy={}", noteId, userId);
        return toResponse(clinicalNoteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> getPatientNotes(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return clinicalNoteRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    private ClinicalNoteResponse toResponse(ClinicalNote n) {
        return new ClinicalNoteResponse(n.getId(), n.getPatientId(), n.getPatientRoundReviewId(),
                n.getVitalsId(), n.getAuthorId(), n.getNoteType(), n.getContent(), n.isAmended(),
                n.getAmendedById(), n.getAmendedAt(), n.getCreatedAt(), n.getUpdatedAt());
    }

    private void validateVitalsLink(String patientId, String vitalsId) {
        if (vitalsId == null || vitalsId.isBlank()) {
            return;
        }
        patientVitalsRepository.findById(vitalsId)
                .filter(vitals -> vitals.getPatientId().equals(patientId))
                .orElseThrow(() -> new ResourceNotFoundException("Vitals record not found for patient"));
    }
}
