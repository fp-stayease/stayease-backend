package com.finalproject.stayease.config;


import com.finalproject.stayease.auth.filter.JwtAuthenticationFilter;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.auth.service.impl.CustomAuthenticationSuccessHandler;
import com.finalproject.stayease.auth.service.impl.CustomOAuth2UserService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Data
public class SecurityConfig {

  private final CorsConfigurationSourceImpl corsConfigurationSource;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;
  private final AuthenticationManagerConfig authenticationManagerConfig;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String googleClientId;
  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String googleClientSecret;
  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String googleRedirectUri;
  @Value("${API_VERSION}")
  private String API_VERSION;


  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .authenticationProvider(authenticationManagerConfig.authenticationProvider())
        .authorizeHttpRequests(this::configureAuthorization)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2Login(this::configureOAuth2Login)
        .formLogin(Customizer.withDefaults())
        .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
            UsernamePasswordAuthenticationFilter.class)
        .logout(Customizer.withDefaults())
        .build();
  }

  private void configureAuthorization(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {

    // Permit all access to certain GET endpoints
    auth.requestMatchers(HttpMethod.GET,
        API_VERSION + "/properties",
        API_VERSION + "/properties/{propertyId}",
        API_VERSION + "/properties/{propertyId}/listings",
        API_VERSION + "/properties/{propertyId}/available/**",
        API_VERSION + "/properties/{propertyId}/rooms/{roomId}",
        API_VERSION + "/properties/{propertyId}/rooms",
        API_VERSION + "/properties/{propertyId}/rooms/{roomId}/available",
        API_VERSION + "/properties/rooms",
        API_VERSION + "/properties/images",
        API_VERSION + "/properties/cities",
        API_VERSION + "/properties/available",
        API_VERSION + "/categories",
        API_VERSION + "/rates",
        API_VERSION + "/rates/daily",
        API_VERSION + "/rates/daily/cumulative",
        API_VERSION + "/payment/{bookingId}",
        API_VERSION + "/reviews/properties/**",
        API_VERSION + "/reviews/rating/**",
        API_VERSION + "/reviews/{reviewId}",
        API_VERSION + "/reviews",
        API_VERSION + "/replies/**"
        ).permitAll();

    auth.requestMatchers(HttpMethod.POST,
            API_VERSION + "/transactions/notification-handler"
    ).permitAll();

    // Permit access to specific POST endpoint
    auth.requestMatchers(HttpMethod.POST,
        API_VERSION + "/transactions",
        API_VERSION + "/reviews"
    ).hasRole("USER");

    // Permit access to specific PUT endpoint
    auth.requestMatchers(HttpMethod.PUT, API_VERSION + "/users/profile/email").permitAll();
    auth.requestMatchers(HttpMethod.PUT,
        API_VERSION + "/transactions/user/{bookingId}",
        API_VERSION + "/reviews/{reviewId}"
    ).hasRole("USER");
    auth.requestMatchers(HttpMethod.PUT,
         API_VERSION + "/transactions/{bookingId}").hasRole("TENANT");

    // Permit access to specific PATCH endpoint
    auth.requestMatchers(HttpMethod.PATCH, API_VERSION + "/transactions/{bookingId}").hasRole("TENANT");

    // Permit access to specific DELETE endpoint
    auth.requestMatchers(HttpMethod.DELETE, API_VERSION + "/reviews/{reviewId}").hasAnyRole("TENANT", "USER");

    // Role-based access control
    auth.requestMatchers(
        API_VERSION + "/payments/payment-proof/{bookingId}",
        API_VERSION + "/bookings/user",
        API_VERSION + "/bookings/upcoming-bookings",
        API_VERSION + "/reports/user-stats",
        API_VERSION + "/reviews/user",
        API_VERSION + "/transactions/user/**"
    ).hasRole("USER");

    auth.requestMatchers(
        API_VERSION + "/properties/**",
        API_VERSION + "/categories/**",
        API_VERSION + "/profile/tenant",
        API_VERSION + "/bookings/tenant",
        API_VERSION + "/rates/auto/**",
        API_VERSION + "/reports/**",
        API_VERSION + "/reviews/tenant",
        API_VERSION + "/replies/**",
        API_VERSION + "/transactions/tenant/**"
    ).hasRole("TENANT");

    // Permit all access to authentication and registration endpoints
    auth.requestMatchers(
        API_VERSION + "/auth/**",
        API_VERSION + "/register/**",
        API_VERSION + "/oauth2/**",
        API_VERSION + "/password/**",
        API_VERSION + "/password/forgot",
        API_VERSION + "/password/reset",
        API_VERSION + "/midtrans",
        API_VERSION + "/bookings/{bookingId}",
        API_VERSION + "/error/**").permitAll();

    // Authenticate any other request
    auth.anyRequest().authenticated();

  }

  private void configureOAuth2Login(OAuth2LoginConfigurer<HttpSecurity> oauth2) {
    oauth2
        .clientRegistrationRepository(clientRegistrationRepository())
        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
        .successHandler(customAuthenticationSuccessHandler);
  }

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    List<ClientRegistration> registrations = new ArrayList<>();
    registrations.add(googleClientRegistration());
    return new InMemoryClientRegistrationRepository(registrations);
  }

  private ClientRegistration googleClientRegistration() {
    return ClientRegistration.withRegistrationId("google")
        .clientId(googleClientId)
        .clientSecret(googleClientSecret)
        .scope("email", "profile")
        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
        .tokenUri("https://oauth2.googleapis.com/token")
        .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
        .userNameAttributeName(IdTokenClaimNames.SUB)
        .clientName("Google")
        .redirectUri(googleRedirectUri)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .build();
  }
}
