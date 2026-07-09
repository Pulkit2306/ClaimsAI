package com.claimsplatform.authservice.security;

import com.claimsplatform.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    @DisplayName("generateToken - token is non-null and parseable")
    void generateToken_returnsValidToken() {
        String token = jwtUtil.generateToken("user@test.com", "ADJUSTER",
                Map.of("userId", 42L));

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@test.com");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADJUSTER");
    }

    @Test
    @DisplayName("isTokenValid - fresh token is valid")
    void isTokenValid_freshToken_true() {
        String token = jwtUtil.generateToken("user@test.com", "ADMIN", Map.of());
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - tampered token is invalid")
    void isTokenValid_tamperedToken_false() {
        String token = jwtUtil.generateToken("user@test.com", "ADMIN", Map.of());
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid - expired token is invalid")
    void isTokenValid_expiredToken_false() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("user@test.com", "ADMIN", Map.of());
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("extractEmail - returns correct subject")
    void extractEmail_returnsSubject() {
        String token = jwtUtil.generateToken("hello@world.com", "POLICYHOLDER", Map.of());
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("hello@world.com");
    }
}
