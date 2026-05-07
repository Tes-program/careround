package com.careround.hospital.ward.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "wards")
public class Ward extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty;

    @Column(name = "total_beds", nullable = false)
    private int totalBeds;

    @Column(name = "supervisor_id", length = 36)
    private String supervisorId;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public int getTotalBeds() { return totalBeds; }
    public void setTotalBeds(int totalBeds) { this.totalBeds = totalBeds; }
    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
}
