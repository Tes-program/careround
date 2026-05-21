package com.careround.patient.handovernote.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "handover_note")
@Getter
@Setter
@NoArgsConstructor
public class HandoverNote extends BaseEntity {

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "author_id", nullable = false, length = 36)
    private String authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
