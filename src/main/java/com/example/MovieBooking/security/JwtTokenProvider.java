package com.example.MovieBooking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // You MUST set these in your application.properties
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtAccessExpirationMs; // Renamed for clarity

    // --- ADD NEW EXPIRATION VALUE ---
    @Value("${app.jwt-refresh-expiration-milliseconds}")
    private long jwtRefreshExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // --- Generate Access Token (Your old generateToken method) ---
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        return generateToken(username, jwtAccessExpirationMs); // Call helper
    }

    public String generateAccessTokenFromEmail(String email) {
        return generateToken(email, jwtAccessExpirationMs); // Call helper
    }
    //-- for Rolling Refresh Token System
    public String generateRefreshTokenFromEmail(String email) {
        return generateToken(email, jwtRefreshExpirationMs);
    }

    // --- ADD NEW METHOD: Generate Refresh Token ---
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        return generateToken(username, jwtRefreshExpirationMs); // Call helper
    }

    // --- CREATE A PRIVATE HELPER METHOD ---
    private String generateToken(String username, long expirationMs) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    // This helper gets the expiry date from a token
    public Date getExpiryDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    // 2. Get the username (email) from the token
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    // 3. Validate the token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parse(token);
            return true;
        } catch (Exception ex) {
            // Can be MalformedJwtException, ExpiredJwtException, etc.
            return false;
        }
    }
}