package com.project.trainingdiary.provider;

import com.project.trainingdiary.entity.BlacklistedTokenEntity;
import com.project.trainingdiary.repository.BlacklistRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

  @Value("${spring.jwt.secret}")
  private String secretKey;

  private Key key;
  private final BlacklistRepository blacklistRepository;

  /**
   * JWT 토큰 생성을 위한 비밀 키를 초기화합니다.
   */
  @PostConstruct
  public void init() {
    if (secretKey == null || secretKey.trim().isEmpty()) {
      throw new IllegalArgumentException("JWT 비밀 키가 올바르게 설정되지 않았습니다.");
    }
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 주어진 사용자 ID와 만료 날짜로 JWT 토큰을 생성합니다.
   */
  public String createToken(String userId, Date expiryDate) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * 사용자 ID로 하루 동안 유효한 접근 토큰을 생성합니다.
   */
  public String createAccessToken(String userId) {
    Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
    return createToken(userId, expiryDate);
  }

  /**
   * 사용자 ID로 7일 동안 유효한 리프레시 토큰을 생성합니다.
   */
  public String createRefreshToken(String userId) {
    Date expiryDate = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
    return createToken(userId, expiryDate);
  }

  /**
   * 주어진 JWT 토큰의 유효성을 검증합니다.
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * JWT 토큰에서 사용자 이름(이메일)을 추출합니다.
   */
  public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  /**
   * JWT 토큰에서 만료 날짜를 추출합니다.
   */
  public Date getExpiryDateFromToken(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    return claims.getExpiration();
  }

  /**
   * 주어진 JWT 토큰이 만료되었는지 확인합니다.
   */
  public boolean isTokenExpired(String token) {
    Date expiryDate = getExpiryDateFromToken(token);
    return expiryDate.before(new Date());
  }

  /**
   * 주어진 JWT 토큰이 블랙리스트에 있는지 확인합니다.
   */
  public boolean isTokenBlacklisted(String token) {
    Optional<BlacklistedTokenEntity> blacklistedToken = blacklistRepository.findByToken(token);
    return blacklistedToken.isPresent();
  }

  /**
   * 주어진 JWT 토큰을 블랙리스트에 추가합니다.
   */
  public void blacklistToken(String token) {
    Date expiryDate = getExpiryDateFromToken(token);
    BlacklistedTokenEntity blacklistedTokenEntity = new BlacklistedTokenEntity();
    blacklistedTokenEntity.setToken(token);
    blacklistedTokenEntity.setExpiryDate(expiryDate);
    blacklistRepository.save(blacklistedTokenEntity);
  }
}