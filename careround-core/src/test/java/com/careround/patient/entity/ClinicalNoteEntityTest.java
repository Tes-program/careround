package com.careround.patient.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClinicalNoteEntityTest {

    @Test
    void defaultIsAiGenerated_shouldBeFalse() {
        ClinicalNote note = new ClinicalNote();
        assertThat(note.isAiGenerated()).isFalse();
    }

    @Test
    void nullableFields_areNullByDefault() {
        ClinicalNote note = new ClinicalNote();
        assertThat(note.getRawTranscription()).isNull();
        assertThat(note.getAiModelUsed()).isNull();
        assertThat(note.getConfirmedByDoctorAt()).isNull();
    }

    @Test
    void hospitalIdField_exists() throws NoSuchFieldException {
        var field = ClinicalNote.class.getDeclaredField("hospitalId");
        assertThat(field).isNotNull();
    }
}
