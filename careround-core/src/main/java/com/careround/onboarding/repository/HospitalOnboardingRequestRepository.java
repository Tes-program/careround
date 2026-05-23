package com.careround.onboarding.repository;

import com.careround.onboarding.entity.HospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalOnboardingRequestRepository extends JpaRepository<HospitalOnboardingRequest, String> {

    boolean existsByContactEmailAndStatus(String contactEmail, HospitalOnboardingStatus status);

    Page<HospitalOnboardingRequest> findByStatus(HospitalOnboardingStatus status, Pageable pageable);

    Optional<HospitalOnboardingRequest> findByContactEmail(String contactEmail);
}
