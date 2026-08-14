package com.hiretrack.hiretrack.security;

import com.hiretrack.hiretrack.entity.User;
import com.hiretrack.hiretrack.util.SecurityUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String secret;

    public JwtService(
            @Value("${app.jwt.secret:hiretracksecretkeyhiretracksecretkey}")
            String secret) {

        this.secret = secret;
    }

    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    public String generateToken(User user) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "role",
                SecurityUtils.normalizeRole(
                        user.getRole()
                )
        );

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 86400000
                        )
                )
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public String extractEmail(
            String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public Claims extractAllClaims(
            String token) {

        return Jwts.parserBuilder()
                .setSigningKey(
                        getSigningKey()
                )
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}