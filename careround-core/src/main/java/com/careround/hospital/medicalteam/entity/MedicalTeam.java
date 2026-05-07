package com.careround.hospital.medicalteam.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "medical_teams")
public class MedicalTeam extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "consultant_id", nullable = false, length = 36)
    private String consultantId;

    @Column(name = "department_id", nullable = false, length = 36)
    private String departmentId;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getConsultantId() { return consultantId; }
    public void setConsultantId(String consultantId) { this.consultantId = consultantId; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
}
