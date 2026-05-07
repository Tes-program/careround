package com.careround.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_team_ward")
@Getter
@Setter
@NoArgsConstructor
public class MedicalTeamWard {

    @EmbeddedId
    private MedicalTeamWardId id;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
}
