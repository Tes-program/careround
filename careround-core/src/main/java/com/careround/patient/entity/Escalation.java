package com.careround.patient.entity;

import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.EscalationTrigger;
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
@Table(name = "escalation")
@Getter
@Setter
@NoArgsConstructor
public class Escalation extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "triggered_by_id", length = 36)
    private String triggeredById;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    private EscalationTrigger triggerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EscalationSeverity severity;

    @Column(name = "assigned_to_id", length = 36)
    private String assignedToId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscalationStatus status = EscalationStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
