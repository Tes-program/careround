package com.careround.common.repository;

import com.careround.common.entity.ClinicalNote;
import com.careround.common.enums.NoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, String> {
    List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<ClinicalNote> findByPatientIdAndNoteType(String patientId, NoteType noteType);
    List<ClinicalNote> findByPatientRoundReviewId(String patientRoundReviewId);
}
