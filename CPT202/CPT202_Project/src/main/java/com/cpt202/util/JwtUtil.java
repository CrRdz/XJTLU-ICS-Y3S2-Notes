package com.cpt202.util;

import com.cpt202.constant.JwtClaimsConstant;
import com.cpt202.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Utility methods for JWT creation and parsing.
 */
public final class JwtUtil {

    private JwtUtil() {
    }

    public static SecretKey createSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret length must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String createToken(SecretKey signingKey, long expirationSeconds, User user) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .claim(JwtClaimsConstant.USER_ID, user.getUserId())
                .claim(JwtClaimsConstant.USERNAME, user.getUsername())
                .claim(JwtClaimsConstant.ROLE, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(signingKey)
                .compact();
    }

    public static Claims parseToken(SecretKey signingKey, String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
