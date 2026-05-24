package com.careround.patient.prescription;

import com.careround.patient.prescription.dto.PrescriptionResponse;
import com.careround.patient.prescription.dto.UpdatePrescriptionRequest;

import java.util.List;

public interface PrescriptionService {

    List<PrescriptionResponse> getActivePrescriptions(String patientId);

    PrescriptionResponse update(String prescriptionId, UpdatePrescriptionRequest request);

    PrescriptionResponse discontinue(String prescriptionId);
}
