package com.careround.patient.clinicalnote;

import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;
import com.careround.patient.entity.ClinicalNote;
import com.careround.patient.repository.ClinicalNoteRepository;
import com.careround.patient.repository.PatientRepository;
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
public class ClinicalNoteServiceImpl implements ClinicalNoteService {

    private final ClinicalNoteRepository clinicalNoteRepository;
    private final PatientRepository patientRepository;

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
        return clinicalNoteRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    private ClinicalNoteResponse toResponse(ClinicalNote n) {
        return new ClinicalNoteResponse(
                n.getId(), n.getPatientId(), n.getHospitalId(), n.getAuthorId(),
                n.getNoteType(), n.getContent(), n.getRawTranscription(),
                n.isAiGenerated(), n.getAiModelUsed(), n.getConfirmedByDoctorAt(),
                n.getCreatedAt(), n.getUpdatedAt());
    }
}
