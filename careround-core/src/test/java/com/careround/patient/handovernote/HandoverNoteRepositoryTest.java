package com.careround.patient.handovernote;

import com.careround.patient.handovernote.entity.HandoverNote;
import com.careround.test.DataJpaH2Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaH2Test
class HandoverNoteRepositoryTest {

    @Autowired
    private HandoverNoteRepository repository;

    private HandoverNote buildNote(String patientId, String hospitalId, String content) {
        HandoverNote note = new HandoverNote();
        note.setPatientId(patientId);
        note.setHospitalId(hospitalId);
        note.setAuthorId("author-1");
        note.setContent(content);
        return note;
    }

    @Test
    void findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc_returnsInDescOrder() {
        HandoverNote first = repository.save(buildNote("patient-1", "hospital-A", "First note"));
        HandoverNote second = repository.save(buildNote("patient-1", "hospital-A", "Second note"));

        List<HandoverNote> results = repository.findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc(
                "patient-1", "hospital-A");

        assertThat(results).hasSize(2);
        // Most recent (second inserted) should be at index 0 since createdAt DESC
        assertThat(results.get(0).getId()).isEqualTo(second.getId());
        assertThat(results.get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc_wrongHospital_returnsEmpty() {
        repository.save(buildNote("patient-1", "hospital-A", "Note"));

        List<HandoverNote> results = repository.findAllByPatientIdAndHospitalIdOrderByCreatedAtDesc(
                "patient-1", "hospital-B");

        assertThat(results).isEmpty();
    }
}
