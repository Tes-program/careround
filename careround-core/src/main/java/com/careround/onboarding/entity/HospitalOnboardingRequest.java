package com.careround.onboarding.entity;

import com.careround.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospital_onboarding_request")
@Getter
@Setter
@NoArgsConstructor
public class HospitalOnboardingRequest extends BaseEntity {

    @Column(name = "hospital_name", nullable = false, length = 255)
    private String hospitalName;

    @Column(name = "country_or_region", nullable = false, length = 120)
    private String countryOrRegion;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "hospital_type", nullable = false, length = 80)
    private String hospitalType;

    @Column(name = "estimated_beds", length = 40)
    private String estimatedInpatientBeds;

    @Column(name = "primary_need", nullable = false, columnDefinition = "TEXT")
    private String primaryNeed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private HospitalOnboardingStatus status = HospitalOnboardingStatus.PENDING_REVIEW;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_by_user_id", length = 36)
    private String reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "provisioned_hospital_id", length = 36)
    private String provisionedHospitalId;
}
