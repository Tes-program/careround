package com.careround.patient.prescription.entity;

import com.careround.patient.prescription.converter.LocalDateTimeListConverter;
import com.careround.patient.prescription.enums.PrescriptionStatus;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescription")
@Getter
@Setter
@NoArgsConstructor
public class Prescription extends BaseEntity {

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "clinical_note_id", length = 36)
    private String clinicalNoteId;

    @Column(name = "drug_name", nullable = false, length = 255)
    private String drugName;

    @Column(nullable = false, length = 50)
    private String dose;

    @Column(nullable = false, length = 50)
    private String route;

    @Column(name = "frequency_string", nullable = false, length = 100)
    private String frequencyString;

    @Column(name = "frequency_hours", nullable = false)
    private int frequencyHours;

    @Column(name = "total_doses", nullable = false)
    private int totalDoses;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Convert(converter = LocalDateTimeListConverter.class)
    @Column(name = "administration_times", nullable = false, columnDefinition = "LONGTEXT")
    private List<LocalDateTime> administrationTimes = new ArrayList<>();

    @Column(name = "confirmed_by_id", nullable = false, length = 36)
    private String confirmedById;

    @Column(name = "confirmed_at", nullable = false)
    private LocalDateTime confirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;
}
