package com.careround.hospital.department.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "head_of_department_id", length = 36)
    private String headOfDepartmentId;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHeadOfDepartmentId() { return headOfDepartmentId; }
    public void setHeadOfDepartmentId(String headOfDepartmentId) { this.headOfDepartmentId = headOfDepartmentId; }
}
