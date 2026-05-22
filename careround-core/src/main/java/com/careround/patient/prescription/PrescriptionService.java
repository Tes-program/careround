package com.careround.patient.prescription;

import com.careround.patient.prescription.dto.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {

    List<PrescriptionResponse> getActivePrescriptions(String patientId);

    PrescriptionResponse discontinue(String prescriptionId);
}
