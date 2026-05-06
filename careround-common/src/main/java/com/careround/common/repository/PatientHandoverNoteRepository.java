package com.careround.common.repository;

import com.careround.common.entity.PatientHandoverNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientHandoverNoteRepository extends JpaRepository<PatientHandoverNote, String> {
    List<PatientHandoverNote> findByHandoverId(String handoverId);
    List<PatientHandoverNote> findByHandoverIdAndPatientId(String handoverId, String patientId);
}
