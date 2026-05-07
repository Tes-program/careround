package com.careround.patient.clinicalnote.repository;

import com.careround.patient.clinicalnote.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, String> {
}

