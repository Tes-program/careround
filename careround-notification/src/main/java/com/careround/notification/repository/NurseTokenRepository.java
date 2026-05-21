package com.careround.notification.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NurseTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<String> findFcmToken(String userId, String hospitalId) {
        String sql = "SELECT fcm_token FROM careround_core.users WHERE id = ? AND hospital_id = ?";
        return jdbcTemplate.query(sql, rs -> rs.next()
                ? Optional.ofNullable(rs.getString("fcm_token"))
                : Optional.<String>empty(), userId, hospitalId);
    }
}
