package com.careround.patient.handovernote;

import com.careround.patient.handovernote.entity.HandoverNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandoverNoteRepository extends JpaRepository<HandoverNote, String> {

    List<HandoverNote> findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc(
            String patientId, String hospitalId);
}
