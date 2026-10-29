package com.acs.crm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final TokenRevocationRegistry revocationRegistry;
    private java.security.PublicKey cachedPublicKey;

    public JwtService(JwtProperties properties, TokenRevocationRegistry revocationRegistry) {
        this.properties = properties;
        this.revocationRegistry = revocationRegistry;
    }

    public UsernamePasswordAuthenticationToken authenticate(String token) {
        boolean rsa = tryPublicKey(token) != null;
        Claims claims = parse(token);
        if (claims.getExpiration() == null || !claims.getExpiration().toInstant().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Expired token");
        }
        // Deactivated, offboarded and deleted users and signed-out sessions, from identity's feed:
        // this service publishes no port of its own any more, but forwarded tokens never pass the
        // gateway's check. Service tokens have no user.
        Object sessionId = claims.get("sessionId");
        if (!(!rsa && isServiceToken(claims)) && revocationRegistry.isRevoked(
                claims.getSubject(),
                claims.getIssuedAt() == null ? null : claims.getIssuedAt().toInstant(),
                sessionId == null ? null : String.valueOf(sessionId))) {
            throw new IllegalArgumentException("Revoked token");
        }

        List<SimpleGrantedAuthority> authorities = Stream.concat(
                stringList(claims, "roles").stream(),
                stringList(claims, "permissions").stream()
            )
            .distinct()
            .map(SimpleGrantedAuthority::new)
            .toList();

        IdentityPrincipal principal = new IdentityPrincipal(
            claims.getSubject(),
            textClaim(claims, "email"),
            textClaim(claims, "name")
        );
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    private Claims parse(String token) {
        Claims viaPublicKey = tryPublicKey(token);
        if (viaPublicKey != null) {
            return viaPublicKey;
        }
        // HMAC with the platform JWT_SECRET, which every service holds. identity signs user tokens
        // with RS256 alone, so only a service's own token (roles exactly [ROLE_SERVICE]) is
        // accepted; a user token here would be forged, and out of reach of revocation.
        Claims legacy = Jwts.parser()
            .verifyWith(signingKey())
            .requireIssuer(properties.getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        if (!isServiceToken(legacy)) {
            throw new IllegalArgumentException("HMAC tokens are accepted for services only");
        }
        return legacy;
    }

    private static boolean isServiceToken(Claims claims) {
        Object roles = claims.get("roles");
        return roles instanceof List<?> list && list.size() == 1 && "ROLE_SERVICE".equals(String.valueOf(list.get(0)));
    }

    /** Returns null rather than throwing, so the caller can fall through to the legacy path. */
    private Claims tryPublicKey(String token) {
        java.security.PublicKey key = publicKey();
        if (key == null) {
            return null;
        }
        try {
            return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception exception) {
            return null;
        }
    }

    private java.security.PublicKey publicKey() {
        if (cachedPublicKey == null) {
            String configured = properties.getPublicKey();
            if (configured == null || configured.isBlank()) {
                return null;
            }
            try {
                byte[] der = java.util.Base64.getDecoder().decode(
                    configured.replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("[^A-Za-z0-9+/=]", ""));
                cachedPublicKey = java.security.KeyFactory.getInstance("RSA")
                    .generatePublic(new java.security.spec.X509EncodedKeySpec(der));
            } catch (Exception exception) {
                throw new IllegalStateException(
                    "app.security.jwt.public-key is not a valid X.509 RSA public key.", exception);
            }
        }
        return cachedPublicKey;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private List<String> stringList(Claims claims, String name) {
        Object value = claims.get(name);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream().map(String::valueOf).toList();
    }

    private String textClaim(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? "" : String.valueOf(value);
    }

    public record IdentityPrincipal(String userId, String email, String name) {
    }
}
