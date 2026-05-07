package com.careround.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MedicalTeamMemberId implements Serializable {

    @Column(name = "medical_team_id", length = 36)
    private String medicalTeamId;

    @Column(name = "user_id", length = 36)
    private String userId;
}
