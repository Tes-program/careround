package com.careround.patient.entity;

import com.careround.patient.enums.AcuityLevel;
import com.careround.patient.enums.AdmissionType;
import com.careround.patient.enums.PatientStatus;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
public class Patient extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "bed_number", length = 20)
    private String bedNumber;

    @Column(name = "medical_team_id", nullable = false, length = 36)
    private String medicalTeamId;

    @Column(name = "admitting_consultant_id", length = 36)
    private String admittingConsultantId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(name = "hospital_number", nullable = false, unique = true, length = 50)
    private String hospitalNumber;

    @Column(name = "admission_date", nullable = false)
    private LocalDateTime admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", nullable = false, length = 20)
    private AdmissionType admissionType;

    @Column(name = "primary_diagnosis", columnDefinition = "TEXT")
    private String primaryDiagnosis;

    @Column(name = "specialty_required", length = 100)
    private String specialtyRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "acuity_level", nullable = false, length = 10)
    private AcuityLevel acuityLevel = AcuityLevel.LOW;

    @Column(name = "news_score", nullable = false)
    private int newsScore = 0;

    @Column(name = "is_discharge_ready", nullable = false)
    private boolean isDischargeReady = false;

    @Column(name = "estimated_discharge_date")
    private LocalDate estimatedDischargeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PatientStatus status = PatientStatus.ADMITTED;
}
