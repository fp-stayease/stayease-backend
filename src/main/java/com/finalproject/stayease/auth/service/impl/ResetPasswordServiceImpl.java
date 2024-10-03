package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;
import com.finalproject.stayease.auth.repository.ResetPasswordRedisRepository;
import com.finalproject.stayease.auth.service.ResetPasswordService;
import com.finalproject.stayease.exceptions.auth.InvalidCredentialsException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.exceptions.auth.PasswordDoesNotMatchException;
import com.finalproject.stayease.exceptions.utils.InvalidTokenException;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
@Transactional
public class ResetPasswordServiceImpl implements ResetPasswordService {

  private final ResetPasswordRedisRepository redisRepository;
  private final UsersService usersService;
  private final SocialLoginService socialLoginService;
  private final MailService mailService;
  private final JwtDecoder jwtDecoder;
  private final JwtEncoder jwtEncoder;
  private final PasswordEncoder passwordEncoder;

  @Value("${FE_URL}")
  private String feUrl;
  @Value("${token.expire.hours:1}")
  private long TOKEN_EXPIRE;

  public ResetPasswordServiceImpl(ResetPasswordRedisRepository redisRepository, UsersService usersService,
      SocialLoginService socialLoginService, MailService mailService, JwtDecoder jwtDecoder, JwtEncoder jwtEncoder,
      PasswordEncoder passwordEncoder) {
    this.redisRepository = redisRepository;
    this.usersService = usersService;
    this.socialLoginService = socialLoginService;
    this.mailService = mailService;
    this.jwtDecoder = jwtDecoder;
    this.jwtEncoder = jwtEncoder;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public ForgotPasswordResponseDTO requestResetToken(ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException {
    String email = requestDTO.getEmail();
    if (redisRepository.isRequested(email)) {
      return handleResendPasswordRequest(email);
    }
    // check if user exist and if they have a social login linked
    checkUser(email);

    return getResetToken(email);
  }

  @Override
  public ForgotPasswordResponseDTO requestResetTokenLoggedIn(ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException {
    String email = requestDTO.getEmail();
    if (redisRepository.isRequested(email)) {
      return handleResendPasswordRequest(email);
    }
    // check if user exist and if they have a social login linked
    checkLoggedInUser(email);
    return getResetToken(email);
  }

  @Override
  public void resetPassword(String randomKey, ResetPasswordRequestDTO requestDTO) {
    String jwtToken = redisRepository.getJwtKey(randomKey);
    String email = jwtDecoder.decode(jwtToken).getSubject();
    checkResetValidity(email, randomKey, requestDTO);

    Users user = usersService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    user.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));
    usersService.save(user);

    redisRepository.blacklist(email, randomKey);
  }

  @Override
  public boolean checkToken(String token) {
    try {
      String jwtToken = redisRepository.getJwtKey(token);
      String email = jwtDecoder.decode(jwtToken).getSubject();
      return redisRepository.isValid(email, token);
    } catch (Exception e) {
      return false;
    }
  }

  public ForgotPasswordResponseDTO getResetToken(String email) throws MessagingException, IOException {

    String randomKey = generateRandomKey(email);

    sendPasswordResetRequestMail(email, randomKey);
    log.info("Request to reset password accepted! Random key: " + randomKey);
    String message = "Request to reset password accepted!";
    return new ForgotPasswordResponseDTO(message, randomKey);
  }

  private void checkResetValidity(String email, String randomKey, ResetPasswordRequestDTO requestDTO) {
    if (email == null) {
      throw new InvalidTokenException("You have not requested to reset your password");
    }
    if (!redisRepository.isValid(email, randomKey)) {
      throw new InvalidRequestException("Request not valid, please send a new request");
    }
    String password = requestDTO.getPassword();
    String confirmPassword = requestDTO.getConfirmPassword();
    if (!password.equals(confirmPassword)) {
      throw new PasswordDoesNotMatchException("Passwords do not match");
    }
  }

  private void checkUser(String email) {
    Optional<Users> usersOptional = usersService.findByEmail(email);
    if (usersOptional.isEmpty()) {
      throw new UsernameNotFoundException("User not found");
    }
    Optional<SocialLogin> socialLoginOptional = socialLoginService.findByUser(usersOptional.get());
    if (socialLoginOptional.isPresent()) {
      throw new InvalidRequestException("Not allowed to change password for social login users");
    }
  }

  private void checkLoggedInUser(String email) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      String emailFromAuthentication = authentication.getName();
      if (!email.equals(emailFromAuthentication)) {
        throw new InvalidCredentialsException("Email does not match");
      }
      checkUser(email);
    }
  }

  private ForgotPasswordResponseDTO handleResendPasswordRequest(String email) throws MessagingException, IOException {
    String randomKey = redisRepository.getTokenFromEmail(email);
    if (!redisRepository.isValid(email, randomKey)) {
      String message = "Your previous request is still valid. Token is: " + randomKey;
      sendPasswordResetRequestMail(email, randomKey);
      return new ForgotPasswordResponseDTO(message, randomKey);
    } else {
      return getResetToken(email);
    }
  }

  private String generateRandomKey(String email) {
    String resetToken = generateResetToken(email);

    String randomKey = UUID.randomUUID().toString();
    redisRepository.saveResetToken(email, randomKey, resetToken);

    return randomKey;
  }

  private String generateResetToken(String email) {
    String jti = UUID.randomUUID().toString().substring(0, 10).replace("-", "").toUpperCase();

    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plus(TOKEN_EXPIRE, ChronoUnit.HOURS))
        .id(jti)
        .subject(email)
        .build();

    var jwt = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet));

    return jwt.getTokenValue();
  }

  private void sendPasswordResetRequestMail(String email, String randomKey) throws IOException, MessagingException {
    // Load the HTML template
    Resource resource = new ClassPathResource("templates/password-reset.html");
    String htmlTemplate = Files.readString(resource.getFile().toPath());
    String url = buildUrl(randomKey);
    log.info("Reset URL: " + url);

    // Replace placeholders with actual values
    String htmlContent = htmlTemplate
        .replace("${resetUrl}", url)
        .replace("{feUrl}", feUrl);

    String subject = "Reset Your Password!";

    mailService.sendHtmlEmail(htmlContent, email, subject);
  }

  private String buildUrl(String token) {
    return feUrl + "/reset-password?token=" + token;
  }

}
