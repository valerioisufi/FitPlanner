package com.example.fitplannerserver.security;

import com.example.fitplannerserver.model.user.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey signingKey;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public JwtUtil() {
        this.signingKey = Jwts.SIG.HS256.key().build();
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    public String generateAccessToken(String userId, Account.Role role) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role.name())
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(15))))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateAccessTokenAndGetClaims(String token) {
        try {
            // parseSignedClaims lancia ExpiredJwtException se il token è scaduto
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return claims.getPayload();
        } catch (Exception e) {
            return Jwts.claims().build();
        }
    }

    public static String generateRefreshToken(){
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        return base64Encoder.encodeToString(randomBytes);
    }
}