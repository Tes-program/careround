package com.careround.patient.vitals;

import com.careround.patient.vitals.dto.RecordVitalsRequest;
import com.careround.patient.vitals.dto.VitalsResponse;

import java.util.List;

public interface PatientVitalsService {

    VitalsResponse recordVitals(String patientId, RecordVitalsRequest request);

    List<VitalsResponse> getVitalsHistory(String patientId, int limit);

    VitalsResponse getLatestVitals(String patientId);
}
