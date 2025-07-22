package com.datashift.datashift_v2.jwtutil;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.datashift.datashift_v2.entity.users.UserEntity;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public Key getSigningKey() {
        String secret = jwtConfig.getSecret();
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserEntity users) {
        return Jwts.builder()
                .setSubject(users.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token or parsing error extracting username", e);
        }
    }

    public String extractEmail(String token) {
        try {
            return getClaims(token).get("email", String.class);
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token or parsing error extracting email", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpirationDate(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public Date extractExpirationDate(String token) {
        try {
            return getClaims(token).getExpiration();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token or parsing error extracting expiration date", e);
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}
