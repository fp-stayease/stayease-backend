package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.auth.service.RegisterRedisService;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.PasswordDoesNotMatchException;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.response.VerifyTenantResponseDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.users.service.PendingRegistrationService;
import com.finalproject.stayease.users.service.RegisterService;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class RegisterServiceImpl implements RegisterService {

  private final UsersService usersService;
  private final TenantInfoService tenantInfoService;
  private final PendingRegistrationService pendingRegistrationService;
  private final RegisterRedisService registerRedisService;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;

  @Value("${BASE_URL}")
  private String baseUrl;
  @Value("${API_VERSION}")
  private String apiVersion;
  @Value("${FE_URL}")
  private String feUrl;


  @Override
  @Transactional
  public InitialRegistrationResponseDTO initialRegistration(InitialRegistrationRequestDTO requestDTO, UserType userType)
      throws RuntimeException, MessagingException, IOException {
    String email = requestDTO.getEmail();
    checkExistingUser(email);
    Optional<PendingRegistration> registration = pendingRegistrationService.findByEmail(email);
    if (registration.isPresent()) {
      return handleExistingRegistration(registration.get(), userType);
    } else {
      return submitRegistration(email, userType);
    }
  }

  @Override
  public Boolean checkToken(String token) {
    String email = registerRedisService.getEmail(token);
    return registerRedisService.isValid(email, token);
  }

  @Override
  public VerifyUserResponseDTO verifyRegistration(VerifyRegistrationDTO verifyRegistrationDTO, String token)
      throws RuntimeException {
    String email = registerRedisService.getEmail(token);
    PendingRegistration pendingRegistration = getPendingRegistration(email);
    registerRedisService.verifyEmail(email, token);
    return createNewUser(pendingRegistration, verifyRegistrationDTO);
  }

  // helpers
  private void checkExistingUser(String email) throws DuplicateEntryException {
    Optional<Users> user = usersService.findByEmail(email);
    if (user.isPresent()) {
      throw new DuplicateEntryException("E-mail already existed! Enter a new e-mail or login");
    }
  }

  @Transactional
  public InitialRegistrationResponseDTO handleExistingRegistration(PendingRegistration pendingRegistration,
      UserType requestedUserType) throws MessagingException, IOException {
    Instant now = Instant.now();

    String email = pendingRegistration.getEmail();
    UserType userType = pendingRegistration.getUserType();

    // if user changed user type
    if (!userType.equals(requestedUserType)) {
      pendingRegistration.setUserType(requestedUserType);
      log.info("User has requested to change user type to {}", requestedUserType);
      String message = "You have requested to change your user type to " + requestedUserType +
                       ". Please verify your email to complete the registration.";
      return resendVerificationEmail(pendingRegistration, message);
    }

    // last pending registration is more than a day old
    if (now.minusSeconds(24 * 60 * 60).getEpochSecond() > pendingRegistration.getCreatedAt().getEpochSecond()) {
      pendingRegistrationService.deleteById(pendingRegistration.getId());
      log.info("User {} has requested to register more than a day ago. The request has been deleted. Registration "
               + "will be resubmitted.", email);
      return submitRegistration(email, userType);
    }

    // last verification email was sent less than an hour before
    if (now.minusSeconds(60 * 60).getEpochSecond() < pendingRegistration.getLastVerificationAttempt()
        .getEpochSecond()) {
      log.info("User {} has requested to register less than an hour ago, resending verification email.", email);
      String message = "Your last verification is still valid, we have resent it to your email at " + email;
      return resendVerificationEmail(pendingRegistration, message);
    }
    return updateAndResendVerificationEmail(pendingRegistration);
  }

  public String generateVerificationToken() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }

  public InitialRegistrationResponseDTO submitRegistration(String email, UserType userType)
      throws DuplicateEntryException, MessagingException, IOException {
    PendingRegistration registration = new PendingRegistration();
    registration.setEmail(email);
    registration.setUserType(userType);
    pendingRegistrationService.save(registration);

    String token = generateAndSaveRedisToken(email);
    log.info("New registration request for {} with email {} has been submitted.", userType, email);
    log.info("Registration token for {} is {}", email, token);

    String message = "Verification link has been sent to " + email + " for registration request as a " + userType + "."
                     + " Please check your e-mail and follow the next steps to verify your account!";

    return sendVerificationEmail(registration, token, message);
  }

  public InitialRegistrationResponseDTO resendVerificationEmail(PendingRegistration pendingRegistration,
      String message) throws MessagingException, IOException {
    String email = pendingRegistration.getEmail();
    String token = registerRedisService.getToken(email);
    log.info("Resending verification email to {}, with token {}", email, token);
    String mailBody =
        "Welcome to StayEase! You recently requested to resend the verification link for your. Click "
        + "this link "
        + "to "
        + "verify your " + pendingRegistration.getUserType() + " account! " + buildVerificationUrl(pendingRegistration.getUserType(), token);

    sendVerificationEmail(pendingRegistration, token, mailBody);

    return registerResponse(message, token);
  }

  public InitialRegistrationResponseDTO updateAndResendVerificationEmail(PendingRegistration pendingRegistration)
      throws MessagingException, IOException {
    pendingRegistration.setLastVerificationAttempt(Instant.now());
    pendingRegistrationService.save(pendingRegistration);
    String email = pendingRegistration.getEmail();
    String token = generateAndSaveRedisToken(email);
    String message =
        "We have updated and resent your new verification link to your email at " + email;
    return sendVerificationEmail(pendingRegistration, token, message);
  }

  public InitialRegistrationResponseDTO sendVerificationEmail(PendingRegistration pendingRegistration, String token,
      String message) throws MessagingException, IOException {

    // Load the HTML template
    Resource resource = new ClassPathResource("templates/verification-email.html");
    String htmlTemplate = Files.readString(resource.getFile().toPath());

    // Get user type
    UserType userType = pendingRegistration.getUserType();

    //Build url
    String url = buildVerificationUrl(userType, token);
    log.info("Verification URL: {}", url);

    // Replace placeholders with actual values
    String htmlContent = htmlTemplate
        .replace("${verificationUrl}", url)
        .replace("${feURL}", feUrl);

    String subject = "Verify your account!";

    mailService.sendHtmlEmail(htmlContent, pendingRegistration.getEmail(), subject);

    return registerResponse(message, token);
  }

  private String buildVerificationUrl(UserType userType, String token) {
    String t = userType == UserType.TENANT ? "t" : "u";
    return feUrl + "/register/verify?t=" + t + "&token=" + token;
  }

  public String generateAndSaveRedisToken(String email) {
    String token = generateVerificationToken();
    registerRedisService.saveVericationToken(email, token);
    return token;
  }

  public InitialRegistrationResponseDTO registerResponse(String message, String token) {
    InitialRegistrationResponseDTO responseDTO = new InitialRegistrationResponseDTO();
    responseDTO.setVerificationToken(token);
    responseDTO.setMessage(message);
    log.info("Request to register successful.");
    return responseDTO;
  }

  // Region - helpers for verification
  PendingRegistration getPendingRegistration(String email) throws RuntimeException {
    Optional<PendingRegistration> pendingRegistrationOptional = pendingRegistrationService.findByEmail(email);
    if (pendingRegistrationOptional.isPresent()) {
      return pendingRegistrationOptional.get();
    } else {
      throw new DataNotFoundException(email + " is not found as a pending registration or it may have expired. "
                                      + "Please submit a new registration request.");
    }
  }

  public VerifyUserResponseDTO createNewUser(PendingRegistration pendingRegistration,
      VerifyRegistrationDTO verifyRegistrationDTO) {
    checkPassword(verifyRegistrationDTO.getPassword(), verifyRegistrationDTO.getConfirmPassword());
    Users user = new Users();
    user.setEmail(pendingRegistration.getEmail());
    user.setUserType(pendingRegistration.getUserType());
    user.setPasswordHash(passwordEncoder.encode(verifyRegistrationDTO.getPassword()));
    user.setFirstName(verifyRegistrationDTO.getFirstName());
    user.setLastName(verifyRegistrationDTO.getLastName());
    user.setPhoneNumber(verifyRegistrationDTO.getPhoneNumber());
    user.setIsVerified(true);
    usersService.save(user);

    // delete pending registration once users are verified
    pendingRegistrationService.deleteById(pendingRegistration.getId());

    if (pendingRegistration.getUserType() == UserType.TENANT) {
      TenantInfo newLandlord = createNewLandlord(verifyRegistrationDTO, user);
      return new VerifyTenantResponseDTO(user, newLandlord);
    } else {
      return new VerifyUserResponseDTO(user);
    }
  }

  public TenantInfo createNewLandlord(VerifyRegistrationDTO verifyRegistrationDTO, Users user) {
    TenantInfo newLandlord = new TenantInfo();
    newLandlord.setUser(user);
    newLandlord.setBusinessName(verifyRegistrationDTO.getBusinessName());
    newLandlord.setTaxId(verifyRegistrationDTO.getTaxId());
    tenantInfoService.save(newLandlord);
    return newLandlord;
  }

  public void checkPassword(String password, String confirmPassword) throws RuntimeException {
    if (!password.equals(confirmPassword)) {
      throw new PasswordDoesNotMatchException("confirmPassword field must be the same as password");
    }
  }
}
