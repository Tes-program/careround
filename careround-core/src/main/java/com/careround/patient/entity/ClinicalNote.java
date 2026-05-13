package com.careround.patient.entity;

import com.careround.patient.enums.NoteType;
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
@Table(name = "clinical_note")
@Getter
@Setter
@NoArgsConstructor
public class ClinicalNote extends BaseEntity {

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "patient_round_review_id", length = 36)
    private String patientRoundReviewId;

    @Column(name = "vitals_id", length = 36)
    private String vitalsId;

    @Column(name = "author_id", nullable = false, length = 36)
    private String authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 25)
    private NoteType noteType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_amended", nullable = false)
    private boolean isAmended = false;

    @Column(name = "amended_by_id", length = 36)
    private String amendedById;

    @Column(name = "amended_at")
    private LocalDateTime amendedAt;
}
