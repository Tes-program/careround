package com.careround.hospital.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient_handover_note")
@Getter
@Setter
@NoArgsConstructor
public class PatientHandoverNote extends BaseEntity {

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
}
