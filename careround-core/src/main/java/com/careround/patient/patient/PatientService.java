package com.careround.patient.patient;

import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.MarkDischargeReadyRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;

import java.util.List;

public interface PatientService {

    PatientResponse admitPatient(AdmitPatientRequest request);

    PatientResponse getPatient(String patientId);

    List<PatientResponse> getPatientsByWard(String wardId);

    List<PatientResponse> searchPatients(String query);

    PatientResponse markDischargeReady(String patientId, MarkDischargeReadyRequest request);

    PatientResponse updatePatientStatus(String patientId, UpdatePatientStatusRequest request);
}
