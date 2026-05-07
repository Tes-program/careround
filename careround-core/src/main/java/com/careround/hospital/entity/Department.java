package com.careround.hospital.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
public class Department extends BaseEntity {

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "head_of_department_id", length = 36)
    private String headOfDepartmentId;
}
