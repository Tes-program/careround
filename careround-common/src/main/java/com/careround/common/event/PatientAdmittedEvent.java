package com.careround.common.event;

/** Published on: PATIENT_ADMITTED → careround.patient.admitted */
public record PatientAdmittedEvent(
        String patientId,
        String wardId,
        String medicalTeamId,
        String hospitalId
) {}
