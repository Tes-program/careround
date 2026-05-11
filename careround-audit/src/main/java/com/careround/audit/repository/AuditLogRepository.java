package com.careround.audit.repository;

import com.careround.audit.entity.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, String> {
    boolean existsByCorrelationId(String correlationId);
}
