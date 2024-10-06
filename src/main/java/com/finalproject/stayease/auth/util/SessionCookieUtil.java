package com.finalproject.stayease.auth.util;

import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;

public class SessionCookieUtil {

  @Value("${token.expiration.refresh:2592000}")
  private static int REFRESH_TOKEN_EXPIRY_IN_SECONDS;

  public static void invalidateSessionAndCookie(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
    }
  }

  public static String extractRefreshToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      return Arrays.stream(cookies)
          .filter(cookie -> "refresh_token".equals(cookie.getName()))
          .findFirst()
          .map(Cookie::getValue)
          .orElse(null);
    }
    return null;
  }

  public static void addRefreshTokenCookie(HttpServletResponse response, TokenResponseDto tokenResponseDto) {
    Cookie cookie = new Cookie("refresh_token", tokenResponseDto.getRefreshToken());
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(REFRESH_TOKEN_EXPIRY_IN_SECONDS);
    response.addCookie(cookie);
    response.setHeader("Authorization", "Bearer " + tokenResponseDto.getAccessToken());
  }
}