package com.careround.patient.medicationchart;

import com.careround.patient.medicationchart.dto.AddManualMedicationRequest;
import com.careround.patient.medicationchart.dto.MedicationChartResponse;
import com.careround.patient.medicationchart.dto.UpdateMedicationChartRequest;

import java.util.List;

public interface MedicationChartService {

    List<MedicationChartResponse> getChartForPatient(String patientId);

    MedicationChartResponse updateNurseNotes(String chartId, UpdateMedicationChartRequest request);

    MedicationChartResponse addManualMedication(String patientId, AddManualMedicationRequest request);

    MedicationChartResponse discontinue(String chartId);
}
