package com.finalproject.stayease.auth.service.helpers;

import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;
import com.finalproject.stayease.auth.repository.ResetPasswordRedisRepository;
import com.finalproject.stayease.exceptions.auth.PasswordDoesNotMatchException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.exceptions.utils.InvalidTokenException;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordHelper {

  private final ResetPasswordRedisRepository redisRepository;
  private final UsersService usersService;
  private final TokenGenerator tokenGenerator;
  private final MailService mailService;
  private final PasswordEncoder passwordEncoder;

  @Value("${FE_URL}")
  private String FE_URL;


  /**
   * Handles the reset token request process.
   */
  public ForgotPasswordResponseDTO handleResetTokenRequest(String email) throws MessagingException, IOException {
    if (redisRepository.isRequested(email)) {
      return handleResendPasswordRequest(email);
    }
    return getResetToken(email);
  }

  /**
   * Performs the actual password reset.
   */
  public void performPasswordReset(String randomKey, ResetPasswordRequestDTO requestDTO) {
    String jwtToken = redisRepository.getJwtKey(randomKey);
    String email = tokenGenerator.getEmailFromToken(jwtToken);
    checkResetValidity(email, randomKey, requestDTO);

    Users user = usersService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    user.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
    usersService.save(user);

    redisRepository.blacklist(email, randomKey);
  }

  private ForgotPasswordResponseDTO handleResendPasswordRequest(String email) throws MessagingException, IOException {
    String randomKey = redisRepository.getTokenFromEmail(email);
    if (redisRepository.isValid(email, randomKey)) {
      String message = "Your previous request is still valid. Token is: " + randomKey;
      sendPasswordResetRequestMail(email, randomKey);
      return new ForgotPasswordResponseDTO(message, randomKey);
    } else {
      return getResetToken(email);
    }
  }

  private ForgotPasswordResponseDTO getResetToken(String email) throws MessagingException, IOException {
    String randomKey = tokenGenerator.generateRandomKey(email);
    sendPasswordResetRequestMail(email, randomKey);
    String message = "Request to reset password accepted!";
    return new ForgotPasswordResponseDTO(message, randomKey);
  }

  private void checkResetValidity(String email, String randomKey, ResetPasswordRequestDTO requestDTO) {
    if (email == null) {
      throw new InvalidTokenException("You have not requested to reset your password");
    }
    if (!redisRepository.isValid(email, randomKey)) {
      log.info("token not valid for: {}", randomKey );
      throw new InvalidRequestException("Request not valid, please send a new request");
    }
    if (!requestDTO.getPassword().equals(requestDTO.getConfirmPassword())) {
      throw new PasswordDoesNotMatchException("Passwords do not match");
    }
  }

  /**
   * Sends a password reset request email.
   */
  private void sendPasswordResetRequestMail(String email, String randomKey) throws IOException, MessagingException {
    ClassPathResource resource = new ClassPathResource("templates/password-reset.html");
    String htmlTemplate;
    try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
      htmlTemplate = FileCopyUtils.copyToString(reader);
    }

    String url = buildUrl(randomKey);
    log.info("Reset URL: {}", url);

    String htmlContent = htmlTemplate
        .replace("${resetUrl}", url)
        .replace("{feUrl}", FE_URL);

    String subject = "Reset Your Password!";

    mailService.sendHtmlEmail(htmlContent, email, subject);
  }

  private String buildUrl(String token) {
    return FE_URL + "/reset-password?token=" + token;
  }
}
