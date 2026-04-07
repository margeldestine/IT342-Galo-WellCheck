package edu.cit.galo.wellcheck.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

// Singleton pattern implementation for JWT token management.

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JwtUtil {

    private final Key key;
    private final long EXPIRATION = 86400000;

    private JwtUtil() {
        this.key = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode("d2VsbGNoZWNrc2VjcmV0a2V5Zm9yand0dG9rZW5nZW5lcmF0aW9ud2VsbGNoZWNr")
        );
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}