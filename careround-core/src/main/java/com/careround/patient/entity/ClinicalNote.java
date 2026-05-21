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

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "author_id", nullable = false, length = 36)
    private String authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 30)
    private NoteType noteType;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "raw_transcription", columnDefinition = "LONGTEXT")
    private String rawTranscription;

    @Column(name = "is_ai_generated", nullable = false)
    private boolean isAiGenerated = false;

    @Column(name = "confirmed_by_doctor_at")
    private LocalDateTime confirmedByDoctorAt;

    @Column(name = "ai_model_used", length = 100)
    private String aiModelUsed;
}
