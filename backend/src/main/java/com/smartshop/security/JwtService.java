package com.smartshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates HS256 JWT access tokens.
 * Rejects application startup if JWT_SECRET is missing or shorter than 32 bytes.
 */
@Service
public class JwtService {

    private static final int MIN_HS256_SECRET_BYTES = 32;

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured and non-blank");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_HS256_SECRET_BYTES) {
            throw new IllegalStateException(
                "JWT_SECRET must be at least " + MIN_HS256_SECRET_BYTES + " bytes for HS256"
            );
        }
        if (jwtProperties.getExpirationMs() <= 0) {
            throw new IllegalStateException("app.jwt.expiration-ms must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String email, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    public Claims parseAndValidateClaims(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        String subject = claims.getSubject();
        String email = claims.get("email", String.class);
        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new JwtException("Missing required JWT claims");
        }
        try {
            Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new JwtException("Invalid subject claim");
        }
        return claims;
    }

    public Long extractUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public long getExpirationMs() {
        return jwtProperties.getExpirationMs();
    }
}
