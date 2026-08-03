package com.renthouse.auth;

import com.renthouse.common.config.AppSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final AppSecurityProperties properties;
    private final SecretKey key;
    public JwtService(AppSecurityProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(decodeSecret(properties.jwtSecret()));
    }
    public String createAccessToken(AppUser user) { return createToken(user, "access", properties.accessTokenMinutes(), ChronoUnit.MINUTES); }
    public String createRefreshToken(AppUser user) { return createToken(user, "refresh", properties.refreshTokenDays(), ChronoUnit.DAYS); }
    private String createToken(AppUser user, String tokenType, long ttl, ChronoUnit unit) {
        Instant now = Instant.now();
        return Jwts.builder().subject(String.valueOf(user.getId())).claim("role", user.getRole().name()).claim("type", tokenType)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(ttl, unit))).signWith(key).compact();
    }
    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = parse(token, "access");
        return new AuthenticatedUser(Long.parseLong(claims.getSubject()), UserRole.valueOf(claims.get("role", String.class)), "");
    }
    public AuthenticatedUser parseRefreshToken(String token) {
        Claims claims = parse(token, "refresh");
        return new AuthenticatedUser(Long.parseLong(claims.getSubject()), UserRole.valueOf(claims.get("role", String.class)), "");
    }
    private Claims parse(String token, String expectedType) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (!expectedType.equals(claims.get("type", String.class))) throw new IllegalArgumentException("令牌类型不正确");
        return claims;
    }
    private byte[] decodeSecret(String value) {
        try { return Base64.getDecoder().decode(value); }
        catch (IllegalArgumentException ignored) { return value.getBytes(StandardCharsets.UTF_8); }
    }
}
