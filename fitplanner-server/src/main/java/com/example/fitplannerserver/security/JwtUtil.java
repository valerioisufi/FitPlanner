package com.example.fitplannerserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey signingKey;

    public JwtUtil() {
        this.signingKey = Jwts.SIG.HS256.key().build();
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + (1000 * 60 * 15))) // 15 minuti
                .signWith(getSigningKey())
                .compact();
    }

    public String validateTokenAndGetUsername(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            if(claims.getPayload().getExpiration().after(new Date())) return null;

            return claims.getPayload().getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
