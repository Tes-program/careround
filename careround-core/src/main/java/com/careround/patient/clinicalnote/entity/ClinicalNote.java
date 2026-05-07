package com.careround.patient.clinicalnote.entity;

import com.careround.shared.enums.NoteType;
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
@Table(name = "clinical_notes")
public class ClinicalNote {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "patient_round_review_id", length = 36)
    private String patientRoundReviewId;

    @Column(name = "author_id", nullable = false, length = 36)
    private String authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false)
    private NoteType noteType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_amended", nullable = false)
    private boolean isAmended = false;

    @Column(name = "amended_by_id", length = 36)
    private String amendedById;

    @Column(name = "amended_at")
    private LocalDateTime amendedAt;

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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
