package com.careround.common.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, nullable = false)
    private String hospitalId;

    @Column(nullable = false)
    private String name;

    /** FK → users.id — nullable since department may not yet have a HOD assigned */
    @Column(name = "head_of_department_id", length = 36)
    private String headOfDepartmentId;

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
    public String getHeadOfDepartmentId() { return headOfDepartmentId; }
    public void setHeadOfDepartmentId(String headOfDepartmentId) { this.headOfDepartmentId = headOfDepartmentId; }
}
