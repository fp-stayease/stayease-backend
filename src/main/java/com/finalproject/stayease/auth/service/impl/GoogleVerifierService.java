package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.dto.AuthResponseDto;
import com.finalproject.stayease.auth.model.dto.OAuth2RegisterRequestDTO;
import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;
import com.finalproject.stayease.auth.service.JwtService;
import com.finalproject.stayease.exceptions.InvalidCredentialsException;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UsersService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Data
@Service
@Slf4j
public class GoogleVerifierService {

  private final GoogleIdTokenVerifier verifier;
  private final SocialLoginService socialLoginService;
  private final UsersService usersService;
  private final JwtService jwtService;

  public GoogleVerifierService(GoogleIdTokenVerifier verifier, SocialLoginService socialLoginService,
      UsersService usersService,
      JwtService jwtService) {
    this.verifier = verifier;
    this.socialLoginService = socialLoginService;
    this.usersService = usersService;
    this.jwtService = jwtService;
  }

  public AuthResponseDto exchangeGoogleToken(String requestToken) {
    log.info("Exchanging google token");
    try {
      // Get user info from token here
      String email = emailFromToken(requestToken);

      // check if user exists
      Users user = usersService.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("User not found"));

      return new AuthResponseDto(user, generateToken(email));
    } catch (Exception e) {
      log.error("Error exchanging google token: " + e.getMessage());
      throw new InvalidCredentialsException("Error exchanging google token");
    }
  }

  public AuthResponseDto registerOAuth2User(OAuth2RegisterRequestDTO requestDTO) {
    log.info("Registering OAuth2 user");
    try {
      Users newUser = registerUser(requestDTO);
      TokenResponseDto token = generateToken(emailFromToken(requestDTO.getGoogleToken()));
      return new AuthResponseDto(newUser, token);
    } catch (Exception e) {
      log.error("Error registering OAuth2 user: " + e.getMessage());
      throw new InvalidCredentialsException("Error registering OAuth2 user");
    }
  }

  private Users registerUser(OAuth2RegisterRequestDTO requestDTO) {
    GoogleIdToken.Payload payload = extractPayload(requestDTO.getGoogleToken());
    String provider = "google";
    String providerUserId = payload.getSubject();
    String email = payload.getEmail();
    String firstName = (String) payload.get("given_name");
    String lastName = (String) payload.get("family_name");
    String avatar = (String) payload.get("picture");
    UserType userType = requestDTO.getUserType();
    String businessName = requestDTO.getBusinessName();
    String taxId = requestDTO.getTaxId();
    SocialLoginRequest socialLoginRequest = new SocialLoginRequest(provider, providerUserId, email, userType, firstName,
        lastName, avatar, businessName, taxId);
    // Register user here
    return socialLoginService.registerOAuth2User(socialLoginRequest);
  }

  private String emailFromToken(String token) {
    GoogleIdToken.Payload payload = extractPayload(token);
    return payload.getEmail();
  }

  private GoogleIdToken.Payload extractPayload(String token) {
    try {
      GoogleIdToken idToken = verifier.verify(token);
      if (idToken == null) {
        log.error("Invalid token");
        throw new InvalidCredentialsException("Invalid token");
      }
      return idToken.getPayload();
    } catch (Exception e) {
      log.error("Error extracting payload: " + e.getMessage());
      throw new InvalidCredentialsException("Error extracting payload");
    }
  }


  private TokenResponseDto generateToken(String email) {
    String accessToken = jwtService.generateAccessTokenFromEmail(email);
    String refreshToken = jwtService.generateRefreshToken(email);
    Long expiresAt = jwtService.getExpiresAt(refreshToken);
    return new TokenResponseDto(accessToken, refreshToken, expiresAt);
  }


//  public SocialLoginRequest extractUserInfoFromToken(GoogleIdToken.Payload payload) {
//    String email = payload.getEmail();
//    String firstName = (String) payload.get("given_name");
//    String lastName = (String) payload.get("family_name");
//    String avatar = (String) payload.get("picture");
//    String provider = "google";
//    String providerUserId = payload.getSubject();
//    return new SocialLoginRequest();
//  }


}
