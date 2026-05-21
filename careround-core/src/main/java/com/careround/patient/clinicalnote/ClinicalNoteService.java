package com.careround.patient.clinicalnote;

import com.careround.patient.clinicalnote.dto.ClinicalNoteResponse;
import com.careround.patient.clinicalnote.dto.CreateClinicalNoteRequest;

import java.util.List;

public interface ClinicalNoteService {
    ClinicalNoteResponse createNote(CreateClinicalNoteRequest request);
    List<ClinicalNoteResponse> getPatientNotes(String patientId);
}
