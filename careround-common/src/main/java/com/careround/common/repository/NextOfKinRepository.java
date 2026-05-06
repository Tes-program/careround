package com.careround.common.repository;

import com.careround.common.entity.NextOfKin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NextOfKinRepository extends JpaRepository<NextOfKin, String> {
    List<NextOfKin> findByPatientId(String patientId);
    Optional<NextOfKin> findByIdAndPatientId(String id, String patientId);

    /** Consent-filtered NOK for notification routing */
    List<NextOfKin> findByPatientIdAndNotificationConsentTrue(String patientId);
}
