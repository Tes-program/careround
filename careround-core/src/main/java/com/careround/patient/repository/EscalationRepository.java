package com.careround.patient.repository;

import com.careround.patient.entity.Escalation;
import com.careround.patient.enums.EscalationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EscalationRepository extends JpaRepository<Escalation, String> {

    List<Escalation> findAllByHospitalIdAndStatusOrderByCreatedAtDesc(
            String hospitalId, EscalationStatus status);

    List<Escalation> findAllByStatusAndCreatedAtBefore(
            EscalationStatus status, LocalDateTime threshold);
}
