package com.careround.common.entity;

import com.careround.common.enums.NoteType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record. Notes are NEVER deleted.
 * Amendments preserve the original: isAmended=true, amendedById set.
 */
@Entity
@Table(name = "clinical_note")
public class ClinicalNote {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "patient_id", length = 36, nullable = false)
    private String patientId;

    /** Nullable — progress notes and ad-hoc notes are not tied to a round review */
    @Column(name = "patient_round_review_id", length = 36)
    private String patientRoundReviewId;

    @Column(name = "author_id", length = 36, nullable = false)
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientRoundReviewId() { return patientRoundReviewId; }
    public void setPatientRoundReviewId(String patientRoundReviewId) { this.patientRoundReviewId = patientRoundReviewId; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public NoteType getNoteType() { return noteType; }
    public void setNoteType(NoteType noteType) { this.noteType = noteType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isAmended() { return isAmended; }
    public void setAmended(boolean amended) { isAmended = amended; }
    public String getAmendedById() { return amendedById; }
    public void setAmendedById(String amendedById) { this.amendedById = amendedById; }
    public LocalDateTime getAmendedAt() { return amendedAt; }
    public void setAmendedAt(LocalDateTime amendedAt) { this.amendedAt = amendedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
