package com.project.trainingdiary.provider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.http.ResponseCookie;
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
  public void setCookie(HttpServletResponse response, String name, String value,
      LocalDateTime expiry, boolean isLocal) {

    long maxAge = Duration.between(LocalDateTime.now(ZoneId.systemDefault()), expiry).getSeconds();

    ResponseCookie responseCookie = createResponseCookieBuilder(name, value, (int) maxAge, isLocal)
        .build();

    response.addHeader("Set-Cookie", responseCookie.toString());
  }

  /**
   * 주어진 이름의 쿠키를 클리어합니다.
   */
  public void clearCookie(HttpServletResponse response, String name, boolean isLocal) {
    ResponseCookie responseCookie = createResponseCookieBuilder(name, null, 0, isLocal)
        .build();

    response.addHeader("Set-Cookie", responseCookie.toString());
  }

  /**
   * ResponseCookieBuilder를 생성합니다.
   */
  private ResponseCookie.ResponseCookieBuilder createResponseCookieBuilder(String name, String value, int maxAge, boolean isLocal) {
    ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
        .httpOnly(true)
        .path("/")
        .maxAge(maxAge);

    if (isLocal) {
      cookieBuilder.sameSite("Lax");
    } else {
      cookieBuilder.secure(true).sameSite("None");
    }

    return cookieBuilder;
  }
}