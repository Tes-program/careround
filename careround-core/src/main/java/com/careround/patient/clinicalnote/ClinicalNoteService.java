package com.careround.patient.clinicalnote;

import com.careround.patient.clinicalnote.dto.AmendNoteRequest;
import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;

import java.util.List;

public interface ClinicalNoteService {
    ClinicalNoteResponse createNote(CreateClinicalNoteRequest request);
    ClinicalNoteResponse amendNote(String noteId, AmendNoteRequest request);
    List<ClinicalNoteResponse> getPatientNotes(String patientId);
}
