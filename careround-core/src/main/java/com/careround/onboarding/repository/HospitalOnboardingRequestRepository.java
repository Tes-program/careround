package com.careround.onboarding.repository;

import com.careround.onboarding.entity.HospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface HospitalOnboardingRequestRepository extends JpaRepository<HospitalOnboardingRequest, String> {

    boolean existsByContactEmailAndStatusIn(String contactEmail, Collection<HospitalOnboardingStatus> statuses);

    List<HospitalOnboardingRequest> findAllByStatusOrderByCreatedAtDesc(
            HospitalOnboardingStatus status, Pageable pageable);

    List<HospitalOnboardingRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<HospitalOnboardingRequest> findAllByStatusAndCreatedAtBeforeOrderByCreatedAtDesc(
            HospitalOnboardingStatus status, LocalDateTime createdAt, Pageable pageable);

    List<HospitalOnboardingRequest> findAllByCreatedAtBeforeOrderByCreatedAtDesc(
            LocalDateTime createdAt, Pageable pageable);
}
