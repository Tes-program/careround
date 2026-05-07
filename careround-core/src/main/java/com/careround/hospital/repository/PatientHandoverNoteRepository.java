package com.careround.hospital.repository;

import com.careround.hospital.entity.PatientHandoverNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientHandoverNoteRepository extends JpaRepository<PatientHandoverNote, String> {

    List<PatientHandoverNote> findAllByHandoverId(String handoverId);
}
