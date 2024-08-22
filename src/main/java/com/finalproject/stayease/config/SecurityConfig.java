package com.finalproject.stayease.config;


import com.finalproject.stayease.auth.filter.JwtAuthenticationFilter;
import com.finalproject.stayease.auth.service.impl.CustomAuthenticationSuccessHandler;
import com.finalproject.stayease.auth.service.impl.CustomOAuth2UserService;
import com.finalproject.stayease.auth.service.impl.UserDetailsServiceImpl;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Data
public class SecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;
//  private final RsaKeyConfigProperties rsaKeyConfigProperties;
//  private final EnvConfigProperties envConfigProperties;
  private final CorsConfigurationSourceImpl corsConfigurationSource;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  private final JwtDecoder jwtDecoder;


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    var authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return new ProviderManager(authProvider);
  }

//  @Bean
//  public JwtEncoder jwtEncoder() throws Exception {
//    var publicKey = rsaKeyConfigProperties.publicKey();
//    var privateKey = rsaKeyConfigProperties.privateKey();
//    if (envConfigProperties.toString().equals("production")) {
//      String publicKeyString = System.getenv("PUBLIC_KEY");
//      String privateKeyString = System.getenv("PRIVATE_KEY");
//
//      publicKey = (RSAPublicKey) parsePublicKey(publicKeyString);
//      privateKey = (RSAPrivateKey) parsePrivateKey(privateKeyString);
//    }
//
//    JWK rsaJwk =
//        new RSAKey.Builder(publicKey).privateKey(privateKey).build();
//    JWKSet jwkSet = new JWKSet(rsaJwk);
//
//    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
//    return new NimbusJwtEncoder(jwkSource);
//  }
//
//  private PublicKey parsePublicKey(String key) throws Exception {
//    String publicKeyPEM = key
//        .replace("-----BEGIN PUBLIC KEY-----", "")
//        .replace("-----END PUBLIC KEY-----", "")
//        .replaceAll("\\s+", "");
//    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
//    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
//    return keyFactory.generatePublic(keySpec);
//  }
//
//  private PrivateKey parsePrivateKey(String key) throws Exception {
//    String privateKeyPEM = key
//        .replace("-----BEGIN PRIVATE KEY-----", "")
//        .replace("-----END PRIVATE KEY-----", "")
//        .replaceAll("\\s+", "");
//    byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
//    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
//    return keyFactory.generatePrivate(keySpec);
//  }
//
//  @Bean
//  public JwtDecoder jwtDecoder() throws Exception {
//    return NimbusJwtDecoder.withPublicKey(rsaKeyConfigProperties.publicKey()).build();
//  }

  // TODO : configure later, only here for starter
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .authorizeHttpRequests(this::configureAuthorization)
        .oauth2Login(this::configureOAuth2Login)
        .formLogin(Customizer.withDefaults())
        .addFilterBefore(new JwtAuthenticationFilter(jwtDecoder), UsernamePasswordAuthenticationFilter.class)
        .logout(Customizer.withDefaults())
        .build();
  }

  private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
    auth.requestMatchers("api/v1/auth/**", "/login/**", "/oauth2/**").permitAll();
    auth.anyRequest().authenticated();
  }

  private void configureOAuth2Login(OAuth2LoginConfigurer<HttpSecurity> oauth2) {
    oauth2
        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // TODO
        .successHandler(customAuthenticationSuccessHandler);
  }
}
