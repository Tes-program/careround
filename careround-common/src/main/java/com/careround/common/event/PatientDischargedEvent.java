package com.careround.common.event;

import java.util.List;

/** Published on: PATIENT_DISCHARGED → careround.patient.discharged */
public record PatientDischargedEvent(
        String patientId,
        String wardId,
        List<String> nokIds,
        String hospitalId
) {}
