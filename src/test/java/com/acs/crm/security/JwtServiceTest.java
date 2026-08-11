package com.acs.crm.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class JwtServiceTest {

    private static final String SECRET = "identity-compatible-test-secret-at-least-32-bytes";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("fawnix-verse");
        properties.setSecret(SECRET);
        jwtService = new JwtService(properties);
    }

    @Test
    void authenticatesIdentityServiceTokenAndAuthorities() {
        String token = token("fawnix-verse");

        UsernamePasswordAuthenticationToken authentication = jwtService.authenticate(token);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities())
            .extracting(Object::toString)
            .containsExactly("ROLE_ADMIN", "b2b.read");
        assertThat(authentication.getPrincipal()).isEqualTo(
            new JwtService.IdentityPrincipal("user-1", "admin@example.com", "Admin User")
        );
    }

    @Test
    void rejectsTokenFromAnotherIssuer() {
        assertThatThrownBy(() -> jwtService.authenticate(token("another-system")))
            .isInstanceOf(RuntimeException.class);
    }

    private String token(String issuer) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject("user-1")
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(300)))
            .claim("email", "admin@example.com")
            .claim("name", "Admin User")
            .claim("roles", List.of("ROLE_ADMIN"))
            .claim("permissions", List.of("b2b.read"))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }
}
