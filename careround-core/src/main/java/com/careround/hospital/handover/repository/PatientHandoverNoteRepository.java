package com.careround.hospital.handover.repository;

import com.careround.hospital.handover.entity.PatientHandoverNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientHandoverNoteRepository extends JpaRepository<PatientHandoverNote, String> {
}

