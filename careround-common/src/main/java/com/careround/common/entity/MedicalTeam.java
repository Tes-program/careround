package com.careround.common.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "medical_team")
public class MedicalTeam {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, nullable = false)
    private String hospitalId;

    @Column(nullable = false)
    private String name;

    /** The lead consultant for this firm; FK → users.id */
    @Column(name = "consultant_id", length = 36, nullable = false)
    private String consultantId;

    /** FK → department.id */
    @Column(name = "department_id", length = 36, nullable = false)
    private String departmentId;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getConsultantId() { return consultantId; }
    public void setConsultantId(String consultantId) { this.consultantId = consultantId; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
}
