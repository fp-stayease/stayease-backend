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
    // Deny all access to the role endpoint
    auth.requestMatchers(API_VERSION + "/role").denyAll();

    // Permit all access to certain GET endpoints
    auth.requestMatchers(HttpMethod.GET,
            API_VERSION + "/properties/{propertyId}",
            API_VERSION + "/properties/{propertyId}/available/**",
            API_VERSION + "/properties/{propertyId}/rooms/{roomId}",
            API_VERSION + "/properties/{propertyId}/rooms/{roomId}/available",
            API_VERSION + "/properties/categories",
            API_VERSION + "/properties/images",
            API_VERSION + "/properties/cities",
            API_VERSION + "/properties/available",
            API_VERSION + "/properties/{propertyId}/rates",
            API_VERSION + "/properties/{propertyId}/rates/daily",
            API_VERSION + "/properties/{propertyId}/rates/daily/cumulative").permitAll();

    // Permit access to specific PUT endpoint
    auth.requestMatchers(HttpMethod.PUT, API_VERSION + "/users/profile/email").permitAll();

    // Role-based access control
    auth.requestMatchers(API_VERSION + "/role/user").hasRole("USER");
    auth.requestMatchers(API_VERSION + "/role/tenant",
//            API_VERSION + "/properties/**",
            API_VERSION + "/profile/tenant").hasRole("TENANT");

    // Permit all access to authentication and registration endpoints
    auth.requestMatchers(API_VERSION + "/auth/**",
            API_VERSION + "/register/**",
            API_VERSION + "/oauth2/**",
            API_VERSION + "/password/**").permitAll();

    // Authenticate any other request
    auth.anyRequest().authenticated();
//    auth.anyRequest().permitAll();
    // TODO !! THIS IS STILL FOR TESTING, PLEASE SECURE THE ENDPOINTS
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
