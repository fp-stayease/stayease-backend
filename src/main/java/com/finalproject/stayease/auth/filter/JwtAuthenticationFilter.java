package com.finalproject.stayease.auth.filter;

import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
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
    try {
      String authHeader = request.getHeader("Authorization");
      String accessToken;
      String email;

      // * 1. check if the auth header is present and starts with "Bearer "
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        log.info("No Bearer token found in request");
        filterChain.doFilter(request, response);
        return;
      }

      // * 2. extract info from authorization HEADER (this is supposed to be access token)
      accessToken = authHeader.substring(7);
      email = jwtService.extractUsername(accessToken);

      // * 3. email is extracted = refresh token w/ email exists
      // * no ctx = not currently logged in with access token
      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // * 4 load userDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // * 5. validate refresh token, and if it's valid..
        if (jwtService.isAccessTokenValid(accessToken, email)) {
          // * 5.1 Create an authentication token
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );

          // * 5.2 Set additional details
          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request)
          );

          // * 5.3 Set the authentication in the SecurityContext
          SecurityContextHolder.getContext().setAuthentication(authToken);
          log.info("(JwtAuthenticationFilter:77) Authentication successful for user {}", email);
        } else {
          log.info("(JwtAuthenticationFilter:79) Invalid username or password for user {}", email);
        }
      }
    } catch (Exception e) {
      log.error("(JwtAuthenticationFilter:83) Authentication failed: " + e.getClass() + ": " + e.getLocalizedMessage());
    }
  // Otherwise, continue the filter chain
    filterChain.doFilter(request, response);
  }
}
