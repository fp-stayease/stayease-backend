package com.finalproject.stayease.auth.filter;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;

  private final static int TOKEN_EXPIRY_IN_SECONDS = 7 * 24 * 60 * 60;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // to make sure it doesn't loop
    if (request.getAttribute("tokenProcessed") != null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String accessToken = extractTokenFromRequest(request);
      if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
        String email = jwtService.extractSubjectFromToken(accessToken);
        if (jwtService.isAccessTokenValid(accessToken, email)) {
          authenticateUser(request, email);
        } else {
          handleExpiredAccessToken(request, response);
        }
      } catch (ExpiredJwtException e) {
          handleExpiredAccessToken(request, response);
        }
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e);
    }
    request.setAttribute("tokenProcessed", true);
    filterChain.doFilter(request, response);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private void authenticateUser(HttpServletRequest request, final String email) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = extractRefreshTokenFromCookie(request);
    if (refreshToken != null) {
      try {
        String email = jwtService.extractSubjectFromToken(refreshToken);
        if (jwtService.isRefreshTokenValid(refreshToken, email)) {
          refreshAndAuthenticateUser(request, response, email);
        }
      } catch (Exception e) {
        log.error("(JwtAuthenticationFilter) Invalid refresh token: {}", e.getLocalizedMessage());
      }
    }
  }

  private String extractRefreshTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() != null) {
      Cookie[] cookies = request.getCookies();
      return Arrays.stream(cookies)
          .filter(cookie -> "refresh_token".equals(cookie.getName()))
          .map(Cookie::getValue)
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  private void refreshAndAuthenticateUser(HttpServletRequest request, HttpServletResponse response, String email) {
    String newAccessToken = jwtService.generateAccessTokenFromEmail(email);
    String newRefreshToken = jwtService.generateRefreshToken(email);
    updateTokensInResponse(response, newAccessToken, newRefreshToken);
    authenticateUser(request, email);
  }

  private void updateTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
    response.setHeader("Authorization", "Bearer " + accessToken);
    Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(TOKEN_EXPIRY_IN_SECONDS);
    response.addCookie(refreshCookie);
  }
}
