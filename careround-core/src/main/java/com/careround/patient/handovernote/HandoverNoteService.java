package com.careround.patient.handovernote;

import com.careround.patient.handovernote.dto.CreateHandoverNoteRequest;
import com.careround.patient.handovernote.dto.HandoverNoteResponse;

import java.util.List;

public interface HandoverNoteService {

    HandoverNoteResponse create(String patientId, CreateHandoverNoteRequest request);

    List<HandoverNoteResponse> list(String patientId);
}
