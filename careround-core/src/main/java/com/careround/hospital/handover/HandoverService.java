package com.careround.hospital.handover;

import com.careround.hospital.handover.dto.AddPatientHandoverNoteRequest;
import com.careround.hospital.handover.dto.CompleteHandoverRequest;
import com.careround.hospital.handover.dto.HandoverResponse;
import com.careround.hospital.handover.dto.InitiateHandoverRequest;
import com.careround.hospital.handover.dto.PatientHandoverNoteResponse;

import java.util.List;

public interface HandoverService {
    HandoverResponse initiateHandover(InitiateHandoverRequest request);
    PatientHandoverNoteResponse addPatientHandoverNote(String handoverId, AddPatientHandoverNoteRequest request);
    HandoverResponse completeHandover(String handoverId, CompleteHandoverRequest request);
    List<HandoverResponse> getHandoversByWard(String wardId);
    List<PatientHandoverNoteResponse> getHandoverNotes(String handoverId);
}
