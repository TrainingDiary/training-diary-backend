package com.project.trainingdiary.provider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class CookieProvider {

  /**
   * 주어진 이름의 쿠키를 요청에서 가져옵니다.
   */
  public Cookie getCookie(HttpServletRequest request, String name) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals(name)) {
          return cookie;
        }
      }
    }
    return null;
  }

  /**
   * 주어진 이름과 값, 만료 날짜로 쿠키를 설정합니다.
   */
  public void setCookie(HttpServletResponse response, String name, String value, LocalDateTime expiry) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true); // 쿠키를 HTTP 전용으로 설정
    cookie.setSecure(true); // 쿠키를 HTTPS에서만 전송하도록 설정
    cookie.setPath("/");
    long maxAge = Duration.between(LocalDateTime.now(ZoneId.systemDefault()), expiry).getSeconds();
    cookie.setMaxAge((int) maxAge);
    response.addCookie(cookie);
  }

  /**
   * 주어진 이름의 쿠키를 클리어합니다.
   */
  public void clearCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setHttpOnly(true); // 쿠키를 HTTP 전용으로 설정
    cookie.setSecure(true); // 쿠키를 HTTPS에서만 전송하도록 설정
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}