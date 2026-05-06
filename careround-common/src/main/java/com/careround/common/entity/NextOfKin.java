package com.careround.common.entity;

import com.careround.common.enums.ContactMethod;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "next_of_kin")
public class NextOfKin {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "patient_id", length = 36, nullable = false)
    private String patientId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 80)
    private String relationship;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", nullable = false, length = 10)
    private ContactMethod preferredContactMethod = ContactMethod.SMS;

    @Column(name = "is_emergency_contact", nullable = false)
    private boolean isEmergencyContact = false;

    @Column(name = "notification_consent", nullable = false)
    private boolean notificationConsent = true;

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public ContactMethod getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(ContactMethod preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }
    public boolean isEmergencyContact() { return isEmergencyContact; }
    public void setEmergencyContact(boolean emergencyContact) { isEmergencyContact = emergencyContact; }
    public boolean isNotificationConsent() { return notificationConsent; }
    public void setNotificationConsent(boolean notificationConsent) { this.notificationConsent = notificationConsent; }
}
