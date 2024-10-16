package com.finalproject.stayease.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class CorsConfigurationSourceImpl implements CorsConfigurationSource {

  @Value("${FE_URL}")
  private String FE_URL;

  @Override
  public CorsConfiguration getCorsConfiguration(@NonNull HttpServletRequest request) {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
    corsConfiguration.setAllowedOriginPatterns(List.of(FE_URL, "http://localhost:3001", "http://localhost:3000",
        "http://0.0.0.0:3000", "http://host.docker.internal:3000"));
    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    corsConfiguration.setAllowCredentials(true);
    corsConfiguration.setExposedHeaders(
        List.of("Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
    return corsConfiguration;
  }
}
