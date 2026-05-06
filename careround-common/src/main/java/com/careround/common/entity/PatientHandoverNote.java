package com.careround.common.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "patient_handover_note")
public class PatientHandoverNote {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "handover_id", length = 36, nullable = false)
    private String handoverId;

    @Column(name = "patient_id", length = 36, nullable = false)
    private String patientId;

    @Column(name = "status_summary", columnDefinition = "TEXT", nullable = false)
    private String statusSummary;

    /** Comma-separated CareTask UUIDs still outstanding at handover */
    @Column(name = "outstanding_task_ids", columnDefinition = "TEXT")
    private String outstandingTaskIds;

    @Column(name = "urgency_flag", nullable = false)
    private boolean urgencyFlag = false;

    @Column(name = "added_by_id", length = 36, nullable = false)
    private String addedById;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
