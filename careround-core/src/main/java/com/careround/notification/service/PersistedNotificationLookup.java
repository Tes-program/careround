package com.careround.notification.service;

import com.careround.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PersistedNotificationLookup {

    private final JdbcTemplate jdbcTemplate;

    public List<PersistedNotification> findForUser(String hospitalId, String userId, UserRole role) {
        try {
            return jdbcTemplate.query("""
                            select id, event_type, recipient_id, recipient_type, channel, subject, body, status, created_at
                            from careround_notification.notifications
                            where hospital_id = ?
                              and (
                                    (recipient_type = 'USER' and recipient_id = ?)
                                 or (recipient_type = 'ROLE' and recipient_id = ?)
                              )
                            order by created_at desc
                            limit 50
                            """,
                    (rs, rowNum) -> new PersistedNotification(
                            rs.getString("id"),
                            rs.getString("event_type"),
                            rs.getString("recipient_id"),
                            rs.getString("recipient_type"),
                            rs.getString("channel"),
                            rs.getString("subject"),
                            rs.getString("body"),
                            rs.getString("status"),
                            toLocalDateTime(rs.getTimestamp("created_at"))),
                    hospitalId,
                    userId,
                    role.name());
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record PersistedNotification(
            String id,
            String eventType,
            String recipientId,
            String recipientType,
            String channel,
            String subject,
            String body,
            String status,
            LocalDateTime createdAt
    ) {}
}
