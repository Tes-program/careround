package com.careround.audit.repository;

import com.careround.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    boolean existsByEventId(String eventId);
    boolean existsByCorrelationId(String correlationId);
}
