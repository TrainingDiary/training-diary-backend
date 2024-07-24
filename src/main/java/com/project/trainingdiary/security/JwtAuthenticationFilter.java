package com.project.trainingdiary.security;

import com.project.trainingdiary.provider.CookieProvider;
import com.project.trainingdiary.provider.TokenProvider;
import com.project.trainingdiary.repository.RedisTokenRepository;
import com.project.trainingdiary.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;
  private final CookieProvider cookieProvider;
  private final UserService userService;
  private final RedisTokenRepository redisTokenRepository;

  private static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
  private static final String ACCESS_TOKEN_COOKIE_NAME = "Access-Token";

  /**
   * JWT 토큰을 검증하는 필터입니다. 접근 토큰이 만료된 경우, 리프레시 토큰을 사용하여 토큰을 재발급 시도합니다.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String accessToken = parseTokenFromRequest(request, ACCESS_TOKEN_COOKIE_NAME);
    String refreshToken = parseTokenFromRequest(request, REFRESH_TOKEN_COOKIE_NAME);

    if (accessToken != null) {
      handleAccessToken(request, response, accessToken, refreshToken);
    } else if (refreshToken != null && tokenProvider.validateToken(refreshToken)) {
      handleRefreshToken(request, response, refreshToken);
    }

    chain.doFilter(request, response);
  }

  /**
   * 접근 토큰을 처리합니다. 유효한 접근 토큰이 있으면 사용자 인증을 수행하고, 만료된 경우 리프레시 토큰을 사용하여 재발급을 시도합니다.
   */
  private void handleAccessToken(HttpServletRequest request, HttpServletResponse response,
      String accessToken, String refreshToken) throws IOException {
    try {
      if (tokenProvider.validateToken(accessToken) && !redisTokenRepository.isAccessTokenValid(
          accessToken)) {
        authenticateUser(accessToken, request);
      } else if (tokenProvider.isTokenExpired(accessToken)) {
        handleExpiredAccessToken(request, response, refreshToken);
      }
    } catch (ExpiredJwtException ex) {
      log.info("접근 토큰이 만료되었습니다. 리프레시 시도 중...");
      handleExpiredAccessToken(request, response, refreshToken);
    }
  }

  /**
   * 만료된 접근 토큰을 처리합니다. 리프레시 토큰이 유효한 경우, 새로운 접근 토큰을 발급합니다.
   */
  private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response,
      String refreshToken) throws IOException {
    if (refreshToken != null && tokenProvider.validateToken(refreshToken)) {
      handleRefreshToken(request, response, refreshToken);
    } else {
      log.warn("유효하지 않거나 누락된 리프레시 토큰");
    }
  }

  /**
   * 리프레시 토큰을 처리합니다. 리프레시 토큰이 유효한 경우, 새로운 접근 토큰을 발급하고 사용자 인증을 수행합니다.
   */
  private void handleRefreshToken(HttpServletRequest request, HttpServletResponse response,
      String refreshToken) throws IOException {
    if (tokenProvider.isTokenBlacklisted(refreshToken)) {
      log.warn("리프레시 토큰이 블랙리스트에 있습니다.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프레시 토큰이 블랙리스트에 있습니다.");
      return;
    }

    String username = tokenProvider.getUsernameFromToken(refreshToken);
    UserDetails userDetails = userService.loadUserByUsername(username);

    if (userDetails != null) {
      String newAccessToken = tokenProvider.createAccessToken(username);
      log.info("새로운 접근 토큰을 쿠키에 설정: {}", newAccessToken);

      boolean isLocal = userService.isLocalRequest(request);

      cookieProvider.setCookie(response, ACCESS_TOKEN_COOKIE_NAME, newAccessToken,
          tokenProvider.getExpiryDateFromToken(newAccessToken), isLocal);
      redisTokenRepository.saveAccessToken(username, newAccessToken,
          tokenProvider.getExpiryDateFromToken(newAccessToken));
      authenticateUser(newAccessToken, request);
    } else {
      log.warn("사용자 정보를 찾을 수 없습니다.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다.");
    }
  }

  /**
   * 사용자 인증을 수행합니다. 주어진 토큰을 사용하여 사용자를 인증하고, 인증 컨텍스트에 설정합니다.
   */
  private void authenticateUser(String token, HttpServletRequest request) {
    String username = tokenProvider.getUsernameFromToken(token);
    UserDetails userDetails = userService.loadUserByUsername(username);
    if (userDetails != null) {
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.info("사용자: {} Role: {}", username, authentication.getAuthorities().toString());
    }
  }

  /**
   * 요청에서 지정된 이름의 쿠키를 파싱합니다. 쿠키가 존재하면 해당 쿠키의 값을 반환합니다.
   */
  private String parseTokenFromRequest(HttpServletRequest request, String cookieName) {
    Cookie tokenCookie = cookieProvider.getCookie(request, cookieName);
    if (tokenCookie != null) {
      return tokenCookie.getValue();
    }
    return null;
  }
}