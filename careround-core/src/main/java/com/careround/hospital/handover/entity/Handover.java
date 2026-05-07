package com.careround.hospital.handover.entity;

import com.careround.shared.enums.HandoverStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "handovers")
public class Handover {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "outgoing_shift_id", nullable = false, length = 36)
    private String outgoingShiftId;

    @Column(name = "incoming_shift_id", nullable = false, length = 36)
    private String incomingShiftId;

    @Column(name = "conducted_by_id", nullable = false, length = 36)
    private String conductedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandoverStatus status = HandoverStatus.PENDING;

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getOutgoingShiftId() { return outgoingShiftId; }
    public void setOutgoingShiftId(String outgoingShiftId) { this.outgoingShiftId = outgoingShiftId; }
    public String getIncomingShiftId() { return incomingShiftId; }
    public void setIncomingShiftId(String incomingShiftId) { this.incomingShiftId = incomingShiftId; }
    public String getConductedById() { return conductedById; }
    public void setConductedById(String conductedById) { this.conductedById = conductedById; }
    public HandoverStatus getStatus() { return status; }
    public void setStatus(HandoverStatus status) { this.status = status; }
    public String getGeneralNotes() { return generalNotes; }
    public void setGeneralNotes(String generalNotes) { this.generalNotes = generalNotes; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
