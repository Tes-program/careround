package com.careround.hospital.entity;

import com.careround.hospital.enums.HandoverStatus;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "handover")
@Getter
@Setter
@NoArgsConstructor
public class Handover extends BaseEntity {

    @Column(name = "ward_id", nullable = false, length = 36)
    private String wardId;

    @Column(name = "outgoing_shift_id", nullable = false, length = 36)
    private String outgoingShiftId;

    @Column(name = "incoming_shift_id", nullable = false, length = 36)
    private String incomingShiftId;

    @Column(name = "conducted_by_id", nullable = false, length = 36)
    private String conductedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HandoverStatus status = HandoverStatus.PENDING;

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
