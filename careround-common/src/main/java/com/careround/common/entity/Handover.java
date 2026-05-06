package com.careround.common.entity;

import com.careround.common.enums.HandoverStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "handover")
public class Handover {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "ward_id", length = 36, nullable = false)
    private String wardId;

    @Column(name = "outgoing_shift_id", length = 36, nullable = false)
    private String outgoingShiftId;

    @Column(name = "incoming_shift_id", length = 36, nullable = false)
    private String incomingShiftId;

    @Column(name = "conducted_by_id", length = 36, nullable = false)
    private String conductedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HandoverStatus status = HandoverStatus.PENDING;

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
}
