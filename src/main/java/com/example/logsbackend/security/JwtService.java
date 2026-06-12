// src/main/java/com/example/logsbackend/security/JwtService.java

package com.example.logsbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:1800000}")
    private long jwtExpiration;

    @Value("${jwt.temp-expiration:300000}")
    private long tempExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes());
    }

    public String generateToken(String username,
                                String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + jwtExpiration))
                .signWith(getKey())
                .compact();
    }

    public String generateTempToken(
            String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "temp")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + tempExpiration))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(
            String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + refreshExpiration))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractType(String token) {
        return parseClaims(token)
                .get("type", String.class);
    }

    public boolean isValidAccessToken(
            String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration()
                    .before(new Date())
                    && "access".equals(
                    claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidRefreshToken(
            String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration()
                    .before(new Date())
                    && "refresh".equals(
                    claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}