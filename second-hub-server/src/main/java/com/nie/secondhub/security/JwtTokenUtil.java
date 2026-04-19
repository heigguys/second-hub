package com.nie.secondhub.security;

import com.nie.secondhub.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenUtil {

    @Resource
    private JwtProperties jwtProperties;

    public String generateToken(Long userId, String role) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(jwtProperties.getExpireSeconds());
        SecretKey secretKey = buildSecretKey(jwtProperties.getSecret());
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .claims(Map.of("role", role))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        SecretKey secretKey = buildSecretKey(jwtProperties.getSecret());
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey buildSecretKey(String rawSecret) {
        byte[] source = rawSecret == null ? new byte[0] : rawSecret.getBytes(StandardCharsets.UTF_8);
        try {
            byte[] hashed = MessageDigest.getInstance("SHA-256").digest(source);
            return Keys.hmacShaKeyFor(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }
}
