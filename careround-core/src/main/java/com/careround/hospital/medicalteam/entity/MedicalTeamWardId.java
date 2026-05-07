package com.careround.hospital.medicalteam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MedicalTeamWardId implements Serializable {

    @Column(name = "medical_team_id", length = 36)
    private String medicalTeamId;

    @Column(name = "ward_id", length = 36)
    private String wardId;

    public MedicalTeamWardId() {}

    public MedicalTeamWardId(String medicalTeamId, String wardId) {
        this.medicalTeamId = medicalTeamId;
        this.wardId = wardId;
    }

    public String getMedicalTeamId() { return medicalTeamId; }
    public void setMedicalTeamId(String medicalTeamId) { this.medicalTeamId = medicalTeamId; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalTeamWardId that = (MedicalTeamWardId) o;
        return Objects.equals(medicalTeamId, that.medicalTeamId) && Objects.equals(wardId, that.wardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalTeamId, wardId);
    }
}
