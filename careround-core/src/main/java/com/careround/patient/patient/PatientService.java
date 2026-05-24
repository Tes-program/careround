package com.careround.patient.patient;

import com.careround.patient.enums.PatientStatus;
import com.careround.patient.patient.dto.AdmitPatientRequest;
import com.careround.patient.patient.dto.PatientResponse;
import com.careround.patient.patient.dto.UpdatePatientStatusRequest;

import java.util.List;

public interface PatientService {

    PatientResponse admitPatient(AdmitPatientRequest request);

    PatientResponse getPatient(String patientId);

    List<PatientResponse> getAllPatients(PatientStatus status, String nameQuery);

    List<PatientResponse> getPatientsByWard(String wardId, String nameQuery);

    PatientResponse updatePatientStatus(String patientId, UpdatePatientStatusRequest request);
}
