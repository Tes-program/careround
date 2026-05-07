package com.careround.hospital.handover.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "patient_handover_notes")
public class PatientHandoverNote {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "handover_id", nullable = false, length = 36)
    private String handoverId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "status_summary", columnDefinition = "TEXT")
    private String statusSummary;

    @Column(name = "outstanding_task_ids", columnDefinition = "TEXT")
    private String outstandingTaskIds;

    @Column(name = "urgency_flag", nullable = false)
    private boolean urgencyFlag = false;

    @Column(name = "added_by_id", nullable = false, length = 36)
    private String addedById;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getHandoverId() { return handoverId; }
    public void setHandoverId(String handoverId) { this.handoverId = handoverId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getStatusSummary() { return statusSummary; }
    public void setStatusSummary(String statusSummary) { this.statusSummary = statusSummary; }
    public String getOutstandingTaskIds() { return outstandingTaskIds; }
    public void setOutstandingTaskIds(String outstandingTaskIds) { this.outstandingTaskIds = outstandingTaskIds; }
    public boolean isUrgencyFlag() { return urgencyFlag; }
    public void setUrgencyFlag(boolean urgencyFlag) { this.urgencyFlag = urgencyFlag; }
    public String getAddedById() { return addedById; }
    public void setAddedById(String addedById) { this.addedById = addedById; }
}
