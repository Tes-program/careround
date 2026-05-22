package com.careround.patient.handovernote;

import com.careround.patient.handovernote.dto.CreateHandoverNoteRequest;
import com.careround.patient.handovernote.dto.HandoverNoteResponse;
import com.careround.patient.handovernote.entity.HandoverNote;
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
public class HandoverNoteServiceImpl implements HandoverNoteService {

    private final HandoverNoteRepository handoverNoteRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public HandoverNoteResponse create(String patientId, CreateHandoverNoteRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        HandoverNote note = new HandoverNote();
        note.setPatientId(patientId);
        note.setHospitalId(hospitalId);
        note.setAuthorId(userId);
        note.setContent(request.content());

        HandoverNote saved = handoverNoteRepository.save(note);
        log.info("action=HANDOVER_NOTE_CREATED patientId={} hospitalId={} authorId={}",
                patientId, hospitalId, userId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HandoverNoteResponse> list(String patientId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        patientRepository.findByIdAndHospitalId(patientId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return handoverNoteRepository
                .findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc(patientId, hospitalId)
                .stream().map(this::toResponse).toList();
    }

    private HandoverNoteResponse toResponse(HandoverNote n) {
        return new HandoverNoteResponse(
                n.getId(), n.getPatientId(), n.getHospitalId(),
                n.getAuthorId(), n.getContent(), n.getCreatedAt());
    }
}
