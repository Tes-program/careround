package com.careround.hospital.handover;

import com.careround.hospital.entity.Handover;
import com.careround.hospital.entity.PatientHandoverNote;
import com.careround.hospital.entity.Shift;
import com.careround.hospital.enums.HandoverStatus;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.handover.dto.AddPatientHandoverNoteRequest;
import com.careround.hospital.handover.dto.CompleteHandoverRequest;
import com.careround.hospital.handover.dto.HandoverResponse;
import com.careround.hospital.handover.dto.InitiateHandoverRequest;
import com.careround.hospital.handover.dto.PatientHandoverNoteResponse;
import com.careround.hospital.repository.HandoverRepository;
import com.careround.hospital.repository.PatientHandoverNoteRepository;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.Patient;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.event.HandoverCompletedEvent;
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
public class HandoverServiceImpl implements HandoverService {

    private final HandoverRepository handoverRepository;
    private final PatientHandoverNoteRepository patientHandoverNoteRepository;
    private final WardRepository wardRepository;
    private final ShiftRepository shiftRepository;
    private final PatientRepository patientRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public HandoverResponse initiateHandover(InitiateHandoverRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        wardRepository.findByIdAndHospitalId(request.wardId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        if (handoverRepository.findByOutgoingShiftId(request.outgoingShiftId()).isPresent())
            throw new BusinessRuleException("A handover already exists for this outgoing shift");

        Shift outgoing = shiftRepository.findById(request.outgoingShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Outgoing shift not found"));
        if (!outgoing.getWardId().equals(request.wardId()))
            throw new AccessDeniedException("Outgoing shift does not belong to this ward");
        if (outgoing.getStatus() != ShiftStatus.ACTIVE)
            throw new BusinessRuleException("Handover can only be initiated from an ACTIVE outgoing shift");

        Shift incoming = shiftRepository.findById(request.incomingShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Incoming shift not found"));
        if (!incoming.getWardId().equals(request.wardId()))
            throw new AccessDeniedException("Incoming shift does not belong to this ward");

        Handover handover = new Handover();
        handover.setWardId(request.wardId());
        handover.setOutgoingShiftId(request.outgoingShiftId());
        handover.setIncomingShiftId(request.incomingShiftId());
        handover.setConductedById(userId);
        handover.setGeneralNotes(request.generalNotes());

        Handover saved = handoverRepository.save(handover);
        log.info("action=initiateHandover handoverId={} wardId={}", saved.getId(), request.wardId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PatientHandoverNoteResponse addPatientHandoverNote(String handoverId, AddPatientHandoverNoteRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();

        Handover handover = handoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found"));
        wardRepository.findByIdAndHospitalId(handover.getWardId(), hospitalId)
                .orElseThrow(() -> new AccessDeniedException("Handover does not belong to this hospital"));
        if (handover.getStatus() == HandoverStatus.COMPLETED)
            throw new BusinessRuleException("Cannot add notes to a completed handover");

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.getWardId().equals(handover.getWardId()))
            throw new BusinessRuleException("Patient is not in the handover ward");

        PatientHandoverNote note = new PatientHandoverNote();
        note.setHandoverId(handoverId);
        note.setPatientId(request.patientId());
        note.setStatusSummary(request.statusSummary());
        note.setOutstandingTaskIds(request.outstandingTaskIds());
        note.setUrgencyFlag(request.urgencyFlag());
        note.setAddedById(userId);

        log.info("action=addPatientHandoverNote handoverId={} patientId={}", handoverId, request.patientId());
        return toNoteResponse(patientHandoverNoteRepository.save(note));
    }

    @Override
    @Transactional
    public HandoverResponse completeHandover(String handoverId, CompleteHandoverRequest request) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Handover handover = handoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found"));
        wardRepository.findByIdAndHospitalId(handover.getWardId(), hospitalId)
                .orElseThrow(() -> new AccessDeniedException("Handover does not belong to this hospital"));
        if (handover.getStatus() == HandoverStatus.COMPLETED)
            throw new BusinessRuleException("Handover is already completed");

        if (request.generalNotes() != null) handover.setGeneralNotes(request.generalNotes());
        handover.setStatus(HandoverStatus.COMPLETED);
        handover.setCompletedAt(LocalDateTime.now(ZoneOffset.UTC));

        shiftRepository.findById(handover.getOutgoingShiftId()).ifPresent(shift -> {
            shift.setStatus(ShiftStatus.HANDED_OVER);
            shiftRepository.save(shift);
        });

        Handover saved = handoverRepository.save(handover);

        outboxService.publish("careround.handover.completed",
                new HandoverCompletedEvent(hospitalId, handover.getId(), handover.getWardId(),
                        handover.getOutgoingShiftId(), handover.getIncomingShiftId(), MDC.get("correlationId")),
                hospitalId);

        log.info("action=completeHandover handoverId={} wardId={}", handoverId, handover.getWardId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HandoverResponse> getHandoversByWard(String wardId) {
        return handoverRepository.findAllByWardIdOrderByCreatedAtDesc(wardId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientHandoverNoteResponse> getHandoverNotes(String handoverId) {
        String hospitalId = HospitalContextHolder.getHospitalId();
        Handover handover = handoverRepository.findById(handoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found"));
        wardRepository.findByIdAndHospitalId(handover.getWardId(), hospitalId)
                .orElseThrow(() -> new AccessDeniedException("Handover does not belong to this hospital"));
        return patientHandoverNoteRepository.findAllByHandoverId(handoverId)
                .stream().map(this::toNoteResponse).toList();
    }

    private HandoverResponse toResponse(Handover h) {
        return new HandoverResponse(h.getId(), h.getWardId(), h.getOutgoingShiftId(),
                h.getIncomingShiftId(), h.getConductedById(), h.getStatus(),
                h.getGeneralNotes(), h.getCompletedAt(), h.getCreatedAt(), h.getUpdatedAt());
    }

    private PatientHandoverNoteResponse toNoteResponse(PatientHandoverNote n) {
        return new PatientHandoverNoteResponse(n.getId(), n.getHandoverId(), n.getPatientId(),
                n.getStatusSummary(), n.getOutstandingTaskIds(), n.isUrgencyFlag(),
                n.getAddedById(), n.getCreatedAt());
    }
}
