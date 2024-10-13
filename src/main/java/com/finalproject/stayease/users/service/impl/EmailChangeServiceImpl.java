package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.auth.repository.AuthRedisRepository;
import com.finalproject.stayease.exceptions.users.UserNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.exceptions.utils.InvalidTokenException;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import com.finalproject.stayease.users.repository.EmailChangeRedisRepository;
import com.finalproject.stayease.users.service.EmailChangeService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
@Transactional
@Data
@Slf4j
public class EmailChangeServiceImpl implements EmailChangeService {

  private final EmailChangeRedisRepository emailChangeRedisRepository;
  private final MailService mailService;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final UsersService usersService;
  private final AuthRedisRepository authRedisRepository;

  @Value("${FE_URL}")
  private String feUrl;
  @Value("${token.expiration.hours:1}")
  private int TOKEN_EXPIRATION_HOURS;

  public EmailChangeServiceImpl(EmailChangeRedisRepository emailChangeRedisRepository, MailService mailService, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, UsersService usersService,
      AuthRedisRepository authRedisRepository) {
    this.emailChangeRedisRepository = emailChangeRedisRepository;
    this.mailService = mailService;
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.usersService = usersService;
    this.authRedisRepository = authRedisRepository;
  }

  /**
   * Initiates an email change request for a user.
   *
   * @param user The user requesting the email change
   * @param requestDTO DTO containing the new email
   * @return A verification URL for the email change
   * @throws MessagingException If there's an error sending the verification email
   * @throws IOException If there's an error reading the email template
   */
  @Override
  public String requestEmailChange(Users user, RequestEmailChangeDTO requestDTO) throws MessagingException, IOException {
    validateEmailChangeRequest(user, requestDTO);

    String emailChangeJwt = generateEmailChangeToken(user, requestDTO.getNewEmail());
    String tokenUUID = UUID.randomUUID().toString().replaceAll("-", "");
    emailChangeRedisRepository.saveToken(tokenUUID, emailChangeJwt);
    sendVerificationEmail(requestDTO.getNewEmail(), tokenUUID);

    return buildVerificationUrl(tokenUUID);
  }

  /**
   * Verifies and processes an email change request.
   *
   * @param tokenUUID The UUID of the email change token
   * @return The updated user with the new email
   * @throws InvalidTokenException If the token is invalid or expired
   */
  @Override
  public Users verifyEmailChange(String tokenUUID) {
    if (!emailChangeRedisRepository.isValid(tokenUUID)) {
      throw new InvalidTokenException("Invalid token");
    }

    String emailChangeJwt = emailChangeRedisRepository.getJwt(tokenUUID);
    String prevEmail = extractSubjectFromToken(emailChangeJwt);
    String newEmail = getNewEmailFromToken(emailChangeJwt);
    log.info("Email change request: {} -> {}", prevEmail, newEmail);

    Users user = getUserByEmail(prevEmail);
    user.setEmail(newEmail);

    manageRedisTokens(prevEmail, tokenUUID);

    return usersService.save(user);
  }

  /**
   * Checks if a given token is valid.
   *
   * @param token The token to check
   * @return true if the token is valid, false otherwise
   */
  @Override
  public boolean checkToken(String token) {
    return emailChangeRedisRepository.isValid(token);
  }

  // Region - Helper methods

  private void sendVerificationEmail(String newEmail, String tokenUUID) throws MessagingException, IOException {
    ClassPathResource resource = new ClassPathResource("templates/email-change.html");
    String htmlTemplate;
    try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
      htmlTemplate = FileCopyUtils.copyToString(reader);
    }

    String url = buildVerificationUrl(tokenUUID);
    String htmlContent = htmlTemplate.replace("${verificationUrl}", url);
    log.info("Email change URL: {}", url);

    mailService.sendHtmlEmail(htmlContent, newEmail, "Verify your new email!");
  }

  private String buildVerificationUrl(String tokenUUID) {
    return feUrl + "/profile/settings/verify-email?token=" + tokenUUID;
  }

  private String generateEmailChangeToken(Users user, String newEmail) {
    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS))
        .subject(user.getEmail())
        .claim("newEmail", newEmail)
        .build();
    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }

  private String extractSubjectFromToken(String token) {
    return jwtDecoder.decode(token).getSubject();
  }

  private String getNewEmailFromToken(String token) {
    return jwtDecoder.decode(token).getClaim("newEmail");
  }

  private Users getUserByEmail(String email) {
    return usersService.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  private void validateEmailChangeRequest(Users user, RequestEmailChangeDTO requestDTO) {
    if (!user.getSocialLogins().isEmpty()) {
      throw new InvalidRequestException("Email change is not allowed for social login users");
    }
    if (requestDTO.getNewEmail() == null || requestDTO.getNewEmail().isEmpty()) {
      throw new InvalidRequestException("New email must not be empty");
    }
    if (usersService.findByEmail(requestDTO.getNewEmail()).isPresent()) {
      throw new InvalidRequestException("Email is already in use");
    }
  }

  private void manageRedisTokens(String prevEmail, String tokenUUID) {
    emailChangeRedisRepository.verifyEmail(tokenUUID);
    if (!authRedisRepository.isRefreshTokenBlacklisted(prevEmail)) {
      authRedisRepository.blacklistKey(prevEmail);
    }
  }
}