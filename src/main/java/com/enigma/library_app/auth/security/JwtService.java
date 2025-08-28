package com.enigma.library_app.auth.security;


import com.enigma.library_app.auth.entity.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String generateToken(String username);
    String buildToken(Map<String, Object> claims, String username);
    Key getSignKey();
    Claims extractAllClaims(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String extractUsername(String token);
    Date extractExpiration(String token);
    Boolean isTokenExpired(String token);
    Boolean validateToken(String token, UserDetails userDetails);
    User getUserFromToken(String token);
    String extractToken(HttpServletRequest request);
    void blacklistToken(String token);
    boolean isTokenBlacklisted(String token);
}
