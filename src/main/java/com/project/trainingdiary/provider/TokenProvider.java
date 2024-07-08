package com.project.trainingdiary.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

  @Value("${spring.jwt.secret}")
  private String secretKey;

  public String createToken(String userId) {
    Date expiredDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
    Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .signWith(key, SignatureAlgorithm.HS256)
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(expiredDate)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  public LocalDateTime getExpiryDateFromToken(String token) {
    Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    Date expiration = claims.getExpiration();
    return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
  }
}
