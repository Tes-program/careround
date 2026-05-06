package com.careround.common.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ward")
public class Ward {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", length = 36, nullable = false)
    private String hospitalId;

    @Column(nullable = false)
    private String name;

    /** Clinical specialty this ward serves (e.g. "Cardiology", "Orthopaedics") */
    @Column(length = 100)
    private String specialty;

    @Column(name = "total_beds", nullable = false)
    private int totalBeds;

    /** FK → users.id — the Ward Supervisor */
    @Column(name = "supervisor_id", length = 36)
    private String supervisorId;

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
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public int getTotalBeds() { return totalBeds; }
    public void setTotalBeds(int totalBeds) { this.totalBeds = totalBeds; }
    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
}
