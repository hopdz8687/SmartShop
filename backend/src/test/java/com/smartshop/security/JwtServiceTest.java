package com.smartshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String VALID_SECRET = "SmartShopTestOnlyJwtSecretKeyForHS256AtLeast32BytesLong!";

    @Test
    void initShouldFailWhenSecretIsMissingOrBlank() {
        JwtProperties missingProps = new JwtProperties();
        missingProps.setSecret(null);
        JwtService missingService = new JwtService(missingProps);
        assertThatThrownBy(missingService::init)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("JWT_SECRET");

        JwtProperties blankProps = new JwtProperties();
        blankProps.setSecret("   ");
        JwtService blankService = new JwtService(blankProps);
        assertThatThrownBy(blankService::init)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void initShouldFailWhenSecretIsTooShortForHs256() {
        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret("too-short-secret");
        JwtService shortService = new JwtService(shortProps);

        assertThatThrownBy(shortService::init)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("32 bytes");
    }

    @Test
    void shouldGenerateAndValidateJwtWithoutSensitiveFields() {
        JwtProperties props = new JwtProperties();
        props.setSecret(VALID_SECRET);
        props.setExpirationMs(60_000L);
        JwtService jwtService = new JwtService(props);
        jwtService.init();

        String token = jwtService.generateToken(42L, "user@smartshop.com", List.of("CUSTOMER"));
        Claims claims = jwtService.parseAndValidateClaims(token);

        assertThat(jwtService.extractUserId(claims)).isEqualTo(42L);
        assertThat(claims.get("email", String.class)).isEqualTo("user@smartshop.com");
        assertThat(claims.get("roles", List.class)).containsExactly("CUSTOMER");
        assertThat(claims.containsKey("password")).isFalse();
        assertThat(claims.containsKey("passwordHash")).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtProperties props = new JwtProperties();
        props.setSecret(VALID_SECRET);
        props.setExpirationMs(60_000L);
        JwtService jwtService = new JwtService(props);
        jwtService.init();

        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
            .subject("10")
            .claim("email", "expired@smartshop.com")
            .claim("roles", List.of("CUSTOMER"))
            .issuedAt(new Date(System.currentTimeMillis() - 120_000L))
            .expiration(new Date(System.currentTimeMillis() - 60_000L))
            .signWith(key, Jwts.SIG.HS256)
            .compact();

        assertThatThrownBy(() -> jwtService.parseAndValidateClaims(expiredToken))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        JwtProperties props = new JwtProperties();
        props.setSecret(VALID_SECRET);
        props.setExpirationMs(60_000L);
        JwtService jwtService = new JwtService(props);
        jwtService.init();

        SecretKey otherKey = Keys.hmacShaKeyFor(
            "AnotherDifferentSecretKeyThatIsAlsoAtLeast32BytesLong!".getBytes(StandardCharsets.UTF_8)
        );
        String forgedToken = Jwts.builder()
            .subject("10")
            .claim("email", "forged@smartshop.com")
            .claim("roles", List.of("ADMIN"))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 60_000L))
            .signWith(otherKey, Jwts.SIG.HS256)
            .compact();

        assertThatThrownBy(() -> jwtService.parseAndValidateClaims(forgedToken))
            .isInstanceOf(JwtException.class);
    }
}
