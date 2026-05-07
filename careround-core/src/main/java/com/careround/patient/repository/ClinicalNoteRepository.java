package com.careround.patient.repository;

import com.careround.patient.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, String> {

    List<ClinicalNote> findAllByPatientIdOrderByCreatedAtDesc(String patientId);
}
