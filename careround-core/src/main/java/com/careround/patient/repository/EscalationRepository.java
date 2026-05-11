package com.careround.patient.repository;

import com.careround.patient.entity.Escalation;
import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.enums.EscalationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EscalationRepository extends JpaRepository<Escalation, String> {

    List<Escalation> findAllByHospitalIdAndStatusOrderByCreatedAtDesc(
            String hospitalId, EscalationStatus status);

    List<Escalation> findAllByStatusAndCreatedAtBefore(
            EscalationStatus status, LocalDateTime threshold);

    List<Escalation> findAllByPatientIdOrderByCreatedAtDesc(String patientId);

    Optional<Escalation> findByIdAndHospitalId(String id, String hospitalId);

    Optional<Escalation> findFirstByPatientIdAndStatusAndSeverityIn(
            String patientId, EscalationStatus status, List<EscalationSeverity> severities);

    List<Escalation> findAllByPatientIdInAndStatusInOrderBySeverityDescCreatedAtAsc(
            List<String> patientIds, List<EscalationStatus> statuses);
}
