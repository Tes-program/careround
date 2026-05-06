package com.careround.common.event;

/** Published on: PATIENT_DETERIORATION → careround.patient.deterioration */
public record PatientDeteriorationEvent(
        String patientId,
        String wardId,
        int newsScore,
        String onCallDoctorId,
        String severity,
        String hospitalId
) {}
