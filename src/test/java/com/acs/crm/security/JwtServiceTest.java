package com.acs.crm.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class JwtServiceTest {

    private static final String SECRET = "identity-compatible-test-secret-at-least-32-bytes";
    private KeyPair identityKeys;
    private TokenRevocationRegistry revocations;
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        identityKeys = generator.generateKeyPair();
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("fawnix-verse");
        properties.setSecret(SECRET);
        properties.setPublicKey(Base64.getEncoder().encodeToString(identityKeys.getPublic().getEncoded()));
        revocations = new TokenRevocationRegistry("http://localhost:1", "", 15000, true);
        revocations.replace(List.of(), List.of());
        jwtService = new JwtService(properties, revocations);
    }

    @Test
    void authenticatesIdentityServiceTokenAndAuthorities() {
        UsernamePasswordAuthenticationToken authentication = jwtService.authenticate(identityToken("fawnix-verse"));

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
        assertThatThrownBy(() -> jwtService.authenticate(identityToken("another-system")))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void anHmacUserTokenIsRefusedEvenWithTheRightSecret() {
        // Every service holds JWT_SECRET; a user token signed with it is a forgery.
        assertThatThrownBy(() -> jwtService.authenticate(hmacToken(List.of("ROLE_ADMIN"))))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void anHmacServiceTokenIsStillAccepted() {
        UsernamePasswordAuthenticationToken authentication = jwtService.authenticate(hmacToken(List.of("ROLE_SERVICE")));

        assertThat(authentication.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_SERVICE");
    }

    @Test
    void aRevokedUserIsRefused() {
        revocations.replace(List.of(new TokenRevocationRegistry.Revocation("user-1", Instant.now())), List.of());

        assertThatThrownBy(() -> jwtService.authenticate(identityToken("fawnix-verse")))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void userTokensAreRefusedUntilRevocationsHaveBeenLoaded() {
        TokenRevocationRegistry unloaded = new TokenRevocationRegistry("http://localhost:1", "", 15000, true);
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("fawnix-verse");
        properties.setSecret(SECRET);
        properties.setPublicKey(Base64.getEncoder().encodeToString(identityKeys.getPublic().getEncoded()));
        JwtService service = new JwtService(properties, unloaded);

        assertThatThrownBy(() -> service.authenticate(identityToken("fawnix-verse")))
            .isInstanceOf(RuntimeException.class);
    }

    private String identityToken(String issuer) {
        Instant now = Instant.now().minusSeconds(5);
        return Jwts.builder()
            .subject("user-1")
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(300)))
            .claim("email", "admin@example.com")
            .claim("name", "Admin User")
            .claim("roles", List.of("ROLE_ADMIN"))
            .claim("permissions", List.of("b2b.read"))
            .signWith(identityKeys.getPrivate())
            .compact();
    }

    private String hmacToken(List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject("forms-service")
            .issuer("fawnix-verse")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(300)))
            .claim("roles", roles)
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }
}
