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

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public UsernamePasswordAuthenticationToken authenticate(String token) {
        Claims claims = parse(token);
        if (claims.getExpiration() == null || !claims.getExpiration().toInstant().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Expired token");
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
        return Jwts.parser()
            .verifyWith(signingKey())
            .requireIssuer(properties.getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
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
