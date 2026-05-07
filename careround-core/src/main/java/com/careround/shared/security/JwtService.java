package com.careround.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs;

    private SecretKey signingKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String userId, String hospitalId, String role) {
        return buildToken(userId, hospitalId, role, accessTokenExpiryMs, "access");
    }

    public String generateRefreshToken(String userId, String hospitalId) {
        return buildToken(userId, hospitalId, null, refreshTokenExpiryMs, "refresh");
    }

    private String buildToken(String userId, String hospitalId, String role, long expiryMs, String tokenType) {
        var now = new Date();
        var claims = Map.of(
                "hospitalId", hospitalId,
                "tokenType", tokenType
        );
        var builder = Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(signingKey());
        if (role != null) {
            builder.claim("role", role);
        }
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
}
