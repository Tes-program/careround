package com.careround.hospital.medicalteam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MedicalTeamMemberId implements Serializable {

    @Column(name = "medical_team_id", length = 36)
    private String medicalTeamId;

    @Column(name = "user_id", length = 36)
    private String userId;

    public MedicalTeamMemberId() {}

    public MedicalTeamMemberId(String medicalTeamId, String userId) {
        this.medicalTeamId = medicalTeamId;
        this.userId = userId;
    }

    public String getMedicalTeamId() { return medicalTeamId; }
    public void setMedicalTeamId(String medicalTeamId) { this.medicalTeamId = medicalTeamId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalTeamMemberId that = (MedicalTeamMemberId) o;
        return Objects.equals(medicalTeamId, that.medicalTeamId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalTeamId, userId);
    }
}
