package com.careround.patient.patient.entity;

import com.careround.shared.entity.BaseEntity;
import com.careround.shared.enums.AcuityLevel;
import com.careround.shared.enums.AdmissionType;
import com.careround.shared.enums.PatientStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {

    @Column(name = "ward_id", length = 36)
    private String wardId;

    @Column(name = "bed_number")
    private String bedNumber;

    @Column(name = "medical_team_id", length = 36)
    private String medicalTeamId;

    @Column(name = "admitting_consultant_id", length = 36)
    private String admittingConsultantId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String gender;

    @Column(name = "hospital_number", unique = true, nullable = false)
    private String hospitalNumber;

    @Column(name = "admission_date", nullable = false)
    private LocalDateTime admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", nullable = false)
    private AdmissionType admissionType;

    @Column(name = "primary_diagnosis")
    private String primaryDiagnosis;

    @Column(name = "specialty_required")
    private String specialtyRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "acuity_level", nullable = false)
    private AcuityLevel acuityLevel = AcuityLevel.LOW;

    @Column(name = "news_score", nullable = false)
    private int newsScore = 0;

    @Column(name = "is_discharge_ready", nullable = false)
    private boolean isDischargeReady = false;

    @Column(name = "estimated_discharge_date")
    private LocalDate estimatedDischargeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientStatus status = PatientStatus.ADMITTED;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public String getMedicalTeamId() { return medicalTeamId; }
    public void setMedicalTeamId(String medicalTeamId) { this.medicalTeamId = medicalTeamId; }
    public String getAdmittingConsultantId() { return admittingConsultantId; }
    public void setAdmittingConsultantId(String admittingConsultantId) { this.admittingConsultantId = admittingConsultantId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getHospitalNumber() { return hospitalNumber; }
    public void setHospitalNumber(String hospitalNumber) { this.hospitalNumber = hospitalNumber; }
    public LocalDateTime getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDateTime admissionDate) { this.admissionDate = admissionDate; }
    public AdmissionType getAdmissionType() { return admissionType; }
    public void setAdmissionType(AdmissionType admissionType) { this.admissionType = admissionType; }
    public String getPrimaryDiagnosis() { return primaryDiagnosis; }
    public void setPrimaryDiagnosis(String primaryDiagnosis) { this.primaryDiagnosis = primaryDiagnosis; }
    public String getSpecialtyRequired() { return specialtyRequired; }
    public void setSpecialtyRequired(String specialtyRequired) { this.specialtyRequired = specialtyRequired; }
    public AcuityLevel getAcuityLevel() { return acuityLevel; }
    public void setAcuityLevel(AcuityLevel acuityLevel) { this.acuityLevel = acuityLevel; }
    public int getNewsScore() { return newsScore; }
    public void setNewsScore(int newsScore) { this.newsScore = newsScore; }
    public boolean isDischargeReady() { return isDischargeReady; }
    public void setDischargeReady(boolean dischargeReady) { isDischargeReady = dischargeReady; }
    public LocalDate getEstimatedDischargeDate() { return estimatedDischargeDate; }
    public void setEstimatedDischargeDate(LocalDate estimatedDischargeDate) { this.estimatedDischargeDate = estimatedDischargeDate; }
    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
