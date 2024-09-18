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



  // TODO : configure later, only here for starter
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

  private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
    auth.requestMatchers(API_VERSION+"/role").denyAll();
    auth.requestMatchers(HttpMethod.GET, API_VERSION+"/properties", API_VERSION+"/properties/rooms").permitAll();
    auth.requestMatchers(HttpMethod.PUT, API_VERSION+"/users/profile/email").permitAll();
    auth.requestMatchers(API_VERSION+"/role/user").hasRole("USER");
    auth.requestMatchers(API_VERSION+"/role/tenant", API_VERSION+"/properties/**", API_VERSION+"/profile/tenant").hasRole("TENANT");
    auth.requestMatchers(API_VERSION+"/auth/**", API_VERSION+"/register/**", API_VERSION+"/oauth2/**", API_VERSION+"/password/**").permitAll();
    auth.requestMatchers(API_VERSION+ "/midtrans").permitAll();
    auth.requestMatchers(API_VERSION+"/payments/payment-proof/{bookingId}").hasRole("USER");
    auth.requestMatchers(HttpMethod.GET, API_VERSION+"/payment/{bookingId}").permitAll();
    auth.requestMatchers(HttpMethod.POST, API_VERSION+"/transactions").hasRole("USER");
    auth.requestMatchers(API_VERSION+"/transactions/notification-handler").permitAll();
    auth.requestMatchers(HttpMethod.PUT, API_VERSION+"/transactions/user/{bookingId}").hasRole("USER");
    auth.requestMatchers(HttpMethod.PUT, API_VERSION+"/transactions/{bookingId}").hasRole("TENANT");
    auth.requestMatchers(HttpMethod.PATCH, API_VERSION+"/transactions/{bookingId}").hasRole("TENANT");
    auth.requestMatchers(API_VERSION+"/bookings/tenant").hasRole("TENANT");
    auth.requestMatchers(API_VERSION+"/bookings/user").hasRole("USER");
    auth.requestMatchers(API_VERSION+"/bookings/{bookingId}").permitAll();
    auth.requestMatchers("/error/**").permitAll();
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
