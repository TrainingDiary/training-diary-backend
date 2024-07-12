package com.project.trainingdiary.repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * Redis에 주어진 키와 토큰을 만료 시간과 함께 저장합니다.
   */
  public void saveToken(String key, String token, LocalDateTime expiryDateTime) {
    long expiration = calculateExpiration(expiryDateTime);
    redisTemplate.opsForValue().set(key, token, expiration, TimeUnit.SECONDS);
  }

  /**
   * Redis에서 주어진 키로 토큰을 삭제합니다.
   */
  public void deleteToken(String key) {
    redisTemplate.delete(key);
  }

  /**
   * Redis에 사용자 ID와 만료 시간과 함께 접근 토큰을 저장합니다.
   */
  public void saveAccessToken(String userId, String token, LocalDateTime expiryDateTime) {
    saveToken("accessToken:" + userId, token, expiryDateTime);
  }

  /**
   * Redis에 사용자 ID와 만료 시간과 함께 리프레시 토큰을 저장합니다.
   */
  public void saveRefreshToken(String userId, String token, LocalDateTime expiryDateTime) {
    saveToken("refreshToken:" + userId, token, expiryDateTime);
  }

  /**
   * 주어진 접근 토큰이 유효한지 확인합니다.
   */
  public boolean isAccessTokenValid(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey("accessToken:" + token));
  }

  /**
   * 주어진 만료 시간을 기준으로 만료 기간을 계산합니다.
   */
  private long calculateExpiration(LocalDateTime expiryDateTime) {
    return expiryDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        - LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
  }
}