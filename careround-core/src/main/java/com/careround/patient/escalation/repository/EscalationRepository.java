package com.careround.patient.escalation.repository;

import com.careround.patient.escalation.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, String> {
}

