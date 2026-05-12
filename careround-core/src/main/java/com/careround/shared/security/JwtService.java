package com.careround.shared.security;

import com.careround.auth.entity.User;
import com.careround.platform.entity.PlatformOperator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

//    @Value("${jwt.refresh-token-expiry-ms:604800000}")
//    private long refreshTokenExpiryMs;

    private SecretKey signingKey() {
//        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return buildToken(user.getId(), user.getEmail(), user.getHospitalId(), user.getRole().name(),
                accessTokenExpiryMs,
                "access");
    }

    public String generatePlatformAccessToken(PlatformOperator operator) {
        return buildToken(operator.getId(), operator.getEmail(), "PLATFORM", operator.getRole().name(),
                accessTokenExpiryMs,
                "access");
    }

//    public String generateRefreshToken(String userId, String email, String hospitalId) {
//        return buildToken(userId, hospitalId, refreshTokenExpiryMs, "refresh");
//    }

    private String buildToken(String userId, String email, String hospitalId, String role, long expiryMs,
                              String tokenType) {
        var now = new Date();
        var claims = Map.of(
                "hospitalId", hospitalId,
                "role", role,
                "email", email,
                "tokenType", tokenType
        );

        var builder = Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(signingKey());

        builder.claim("role", role);
        return builder.compact();
    }

    public Claims validateAndParseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("tokenType", String.class));
    }

    public boolean isTokenValid(String token) {
        if (!StringUtils.hasText(token)) return false;
        try {
            Claims claims = validateAndParseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return validateAndParseClaims(token).getSubject();
    }

    public String extractHospitalId(String token) {
        return validateAndParseClaims(token).get("hospitalId", String.class);
    }

    public String extractRole(String token) {
        return validateAndParseClaims(token).get("role", String.class);
    }

    public Long getAccessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }
}
