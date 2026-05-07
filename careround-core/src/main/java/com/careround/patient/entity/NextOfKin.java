package com.careround.patient.entity;

import com.careround.patient.enums.ContactMethod;
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
@Table(name = "next_of_kin")
@Getter
@Setter
@NoArgsConstructor
public class NextOfKin extends BaseEntity {

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String relationship;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", nullable = false, length = 10)
    private ContactMethod preferredContactMethod = ContactMethod.SMS;

    @Column(name = "is_emergency_contact", nullable = false)
    private boolean isEmergencyContact = false;

    @Column(name = "notification_consent", nullable = false)
    private boolean notificationConsent = false;
}
