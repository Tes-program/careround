package com.careround.patient.nextofkin.entity;

import com.careround.shared.enums.ContactMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "next_of_kin")
public class NextOfKin {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "hospital_id", nullable = false, length = 36)
    private String hospitalId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String relationship;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", nullable = false)
    private ContactMethod preferredContactMethod = ContactMethod.SMS;

    @Column(name = "is_emergency_contact", nullable = false)
    private boolean isEmergencyContact = false;

    @Column(name = "notification_consent", nullable = false)
    private boolean notificationConsent = true;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
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
