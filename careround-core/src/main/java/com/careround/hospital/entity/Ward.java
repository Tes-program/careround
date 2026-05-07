package com.careround.hospital.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ward")
@Getter
@Setter
@NoArgsConstructor
public class Ward extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String specialty;

    @Column(name = "total_beds", nullable = false)
    private int totalBeds = 0;

    @Column(name = "supervisor_id", length = 36)
    private String supervisorId;
}
