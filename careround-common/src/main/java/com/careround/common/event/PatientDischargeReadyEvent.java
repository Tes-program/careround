package com.careround.common.event;

import java.time.LocalDate;

/** Published on: PATIENT_DISCHARGE_READY → careround.patient.discharge-ready */
public record PatientDischargeReadyEvent(
        String patientId,
        String wardId,
        LocalDate estimatedDischargeDate,
        String hospitalId
) {}
