package com.careround.patient.medicationchart.entity;

import com.careround.patient.medicationchart.enums.MedicationChartStatus;
import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medication_chart")
@Getter
@Setter
@NoArgsConstructor
public class MedicationChart extends BaseEntity {

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "prescription_id", nullable = false, length = 36)
    private String prescriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicationChartStatus status = MedicationChartStatus.ACTIVE;

    @Column(name = "nurse_notes", columnDefinition = "TEXT")
    private String nurseNotes;
}
