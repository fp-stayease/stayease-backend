package com.finalproject.stayease.auth.filter;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.BadJwtException;
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

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // to make sure it doesn't loop
    if (request.getAttribute("tokenProcessed") != null || request.getRequestURI().equals("/api/v1/auth/refresh") ||
        request.getRequestURI().equals("/api/v1/auth/refresh-access")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String accessToken = extractTokenFromRequest(request);
      log.info("Extracted token: {}", accessToken != null ? "Token present" : "Token absent");

      if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
          log.info("Attempting to extract subject from token");
          String email = jwtService.extractSubjectFromToken(accessToken);
          log.info("Extracted email: {}", email);

          log.info("Checking if access token is valid");
          if (jwtService.isAccessTokenValid(accessToken, email)) {
            log.info("Access token is valid, authenticating user");
            authenticateUser(request, email);
          } else {
            log.info("Token is invalid or expired");
            filterChain.doFilter(request, response);
          }
        } catch (ExpiredJwtException e) {
          log.error("Caught ExpiredJwtException", e);
          filterChain.doFilter(request, response);
        } catch (BadJwtException e) {
          logger.error("Caught BadJwtException: Token is malformed", e);
          filterChain.doFilter(request, response);
        }
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication: " + e.getClass() + ": " + e.getLocalizedMessage());
    }
    request.setAttribute("tokenProcessed", true);
    filterChain.doFilter(request, response);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    log.info("bearer token: {}", bearerToken);
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
    log.info("Authenticated user: " + userDetails.getUsername());
  }
}
