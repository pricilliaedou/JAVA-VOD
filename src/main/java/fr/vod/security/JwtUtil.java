package fr.vod.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:changeitchangethiskeychangethiskey1234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + jwtExpirationMs);

            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiry)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (NoClassDefFoundError e) {
            throw new IllegalStateException("JJWT classes not found at runtime. Add jjwt-api, jjwt-impl and jjwt-jackson to your classpath.", e);
        }
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (NoClassDefFoundError e) {
            throw new IllegalStateException("JJWT classes not found at runtime. Add jjwt-api, jjwt-impl and jjwt-jackson to your classpath.", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (NoClassDefFoundError e) {
            // Convert linkage error into clear runtime exception so filter can handle or log it
            throw new IllegalStateException("JJWT classes not found at runtime. Add jjwt-api, jjwt-impl and jjwt-jackson to your classpath.", e);
        } catch (Exception ex) {
            return false;
        }
    }
}