package com.finalproject.stayease.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtProviderConfig {

  private final RsaKeyConfigProperties rsaKeyConfigProperties;
  private final EnvConfigProperties envConfigProperties;

  public JwtProviderConfig(RsaKeyConfigProperties rsaKeyConfigProperties, EnvConfigProperties envConfigProperties) {
    this.rsaKeyConfigProperties = rsaKeyConfigProperties;
    this.envConfigProperties = envConfigProperties;
  }

  @Bean
  public JwtEncoder jwtEncoder() throws Exception {
    var publicKey = rsaKeyConfigProperties.publicKey();
    var privateKey = rsaKeyConfigProperties.privateKey();
    if (envConfigProperties.toString().equals("production")) {
      String publicKeyString = System.getenv("PUBLIC_KEY");
      String privateKeyString = System.getenv("PRIVATE_KEY");

      publicKey = (RSAPublicKey) parsePublicKey(publicKeyString);
      privateKey = (RSAPrivateKey) parsePrivateKey(privateKeyString);
    }

    JWK rsaJwk =
        new RSAKey.Builder(publicKey).privateKey(privateKey).build();
    JWKSet jwkSet = new JWKSet(rsaJwk);

    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
    return new NimbusJwtEncoder(jwkSource);
  }

  private PublicKey parsePublicKey(String key) throws Exception {
    String publicKeyPEM = key
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s+", "");
    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    return keyFactory.generatePublic(keySpec);
  }

  private PrivateKey parsePrivateKey(String key) throws Exception {
    String privateKeyPEM = key
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s+", "");
    byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    return keyFactory.generatePrivate(keySpec);
  }

  @Bean
  public JwtDecoder jwtDecoder() throws Exception {
    return NimbusJwtDecoder.withPublicKey(rsaKeyConfigProperties.publicKey()).build();
  }

}
