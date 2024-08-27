package com.finalproject.stayease.auth.filter;

import com.finalproject.stayease.auth.model.dto.LoginResponseDTO;
import com.finalproject.stayease.auth.service.AuthService;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
  private final AuthService authService;
  private final UserDetailsServiceImpl userDetailsService;

  // TODO : Investigate why JwtFilter is being triggered repeatedly

  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//      throws ServletException, IOException {
//    try {
//      // TODO delete when done !! logging purposes
//      String requestId = UUID.randomUUID().toString();
//      log.info("Processing request {} in JwtAuthenticationFilter", requestId);
//
//      // Check if token has already been processed in this request
//      if (Boolean.TRUE.equals(request.getAttribute("tokenProcessed"))) {
//        filterChain.doFilter(request, response);
//        return;
//      }
//
//      String authHeader = request.getHeader("Authorization");
//      String accessToken;
//      String email;
//
//      // * 1. check if the auth header is present and starts with "Bearer "
//      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//        log.info("No Bearer token found in request");
//        filterChain.doFilter(request, response);
//        return;
//      }
//
//      // * 2. extract info from authorization HEADER (this is supposed to be access token)
//      accessToken = authHeader.substring(7);
//      boolean tokenProcessed = false;
//
//      try {
//        email = jwtService.extractSubjectFromToken(request, accessToken);
//      } catch (JwtException e) {
//        log.warn("Expired access token, proceeding to refresh mechanism for request: {}", request.getRequestURI());
//        LoginResponseDTO refreshResponse = authService.refreshToken(request, response);
//        log.info("Token refreshed successfully for request: {} with requestId in JwtAuthenticationFilter: {}", request.getRequestURI(),
//            requestId);
//        accessToken =  refreshResponse.getAccessToken();
//        String newRefreshToken = refreshResponse.getRefreshToken();
//        email = jwtService.decodeToken(newRefreshToken).getSubject();
//        tokenProcessed= true;
//
//
//      } catch (Exception e) {
//        log.info("Invalid JWT token: " + e.getLocalizedMessage());
//        filterChain.doFilter(request, response);
//        return;
//      }
//
//      // * 3. email is extracted = refresh token w/ email exists
//      // * no ctx = not currently logged in with access token
//      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//        // * 4 load userDetails
//        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//
//        // * 5. validate access token, and if it's valid..
//        if (tokenProcessed || jwtService.isAccessTokenValid(accessToken, email)) {
//          // * 5.1 Create an authentication token
//          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//              userDetails,
//              null,
//              userDetails.getAuthorities()
//          );
//
//          // * 5.2 Set additional details
//          authToken.setDetails(
//              new WebAuthenticationDetailsSource().buildDetails(request)
//          );
//
//          // * 5.3 Set the authentication in the SecurityContext
//          SecurityContextHolder.getContext().setAuthentication(authToken);
//          log.info("(JwtAuthenticationFilter:77) Authentication successful for user {}", email);
//          tokenProcessed = true;
//        } else {
//          log.info("(JwtAuthenticationFilter:79) Invalid username or password for user {}", email);
//        }
//      }
//
//      request.setAttribute("tokenProcessed", true);
//      // this is where the filter would loop
//        filterChain.doFilter(request, response);
//
//
//
//    } catch (Exception e) {
//      log.error("(JwtAuthenticationFilter:91) Authentication failed: " + e.getClass() + ": " + e.getLocalizedMessage());
//    }
//  }

  // Region -
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      String requestId = UUID.randomUUID().toString();
      log.info("Processing request {} in JwtAuthenticationFilter", requestId);

      if (Boolean.TRUE.equals(request.getAttribute("tokenProcessed"))) {
        filterChain.doFilter(request, response);
        return;
      }

      String authHeader = request.getHeader("Authorization");
      if (!isBearerToken(authHeader)) {
        filterChain.doFilter(request, response);
        return;
      }

      String accessToken = getTokenFromHeader(authHeader);
      String email;
      boolean tokenProcessed = false;

      try {
        email = jwtService.extractSubjectFromToken(request, accessToken);
      } catch (JwtException e) {
        email = handleTokenRefresh(request, response, filterChain);
        tokenProcessed = true;

        // Wrap the request to replace the Authorization header with the new access token
        request = wrapRequestWithNewAccessToken(request, accessToken);
      } catch (Exception e) {
        log.info("Invalid JWT token: " + e.getLocalizedMessage());
        filterChain.doFilter(request, response);
        return;
      }

      if (email != null && isAuthenticationAbsent()) {
        authenticateUser(request, email, accessToken, tokenProcessed);
      }

      request.setAttribute("tokenProcessed", true);
      filterChain.doFilter(request, response);

    } catch (Exception e) {
      log.error("Authentication failed: " + e.getClass() + ": " + e.getLocalizedMessage());
    }
  }

  private boolean isBearerToken(String authHeader) {
    return authHeader != null && authHeader.startsWith("Bearer ");
  }

  private String getTokenFromHeader(String authHeader) {
    return authHeader.substring(7);
  }

  private String handleTokenRefresh(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    log.info("Expired access token, attempting to refresh token for request: {}", request.getRequestURI());
    LoginResponseDTO refreshResponse = authService.refreshToken(request, response);
    log.info("Token refreshed successfully for request: {}", request.getRequestURI());
    return jwtService.decodeToken(refreshResponse.getRefreshToken()).getSubject();
  }

  private HttpServletRequest wrapRequestWithNewAccessToken(HttpServletRequest request, String accessToken) {
    return new HttpServletRequestWrapper(request) {
      @Override
      public String getHeader(String name) {
        if ("Authorization".equalsIgnoreCase(name)) {
          return "Bearer " + accessToken;
        }
        return super.getHeader(name);
      }

      @Override
      public Enumeration<String> getHeaders(String name) {
        if ("Authorization".equalsIgnoreCase(name)) {
          return Collections.enumeration(Collections.singleton("Bearer " + accessToken));
        }
        return super.getHeaders(name);
      }

      @Override
      public Enumeration<String> getHeaderNames() {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(Collections.list(super.getHeaderNames()).stream()
            .collect(Collectors.toMap(h -> h, h -> super.getHeader(h))));
        headers.put("Authorization", "Bearer " + accessToken);
        return Collections.enumeration(headers.keySet());
      }
    };
  }
  private boolean isAuthenticationAbsent() {
    return SecurityContextHolder.getContext().getAuthentication() == null;
  }

  private void authenticateUser(HttpServletRequest request, String email, String accessToken, boolean tokenProcessed) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    if (tokenProcessed || jwtService.isAccessTokenValid(accessToken, email)) {
      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
      log.info("Authentication successful for user {}", email);
    } else {
      log.info("Invalid token or user authentication failed for user {}", email);
    }
  }
}
