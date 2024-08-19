package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.auth.service.RegisterRedisService;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.PasswordDoesNotMatchException;
import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.response.VerifyTenantResponseDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.users.repository.PendingRegistrationRepository;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.RegisterService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class RegisterServiceImpl implements RegisterService {

  private final UserRepository userRepository;
  private final TenantInfoRepository tenantInfoRepository;
  private final PendingRegistrationRepository registrationRepository;
  private final RegisterRedisService registerRedisService;
  private final PasswordEncoder passwordEncoder;


  @Override
  @Transactional
  public InitialRegistrationResponseDTO initialRegistration(InitialRegistrationRequestDTO requestDTO, UserType userType)
      throws RuntimeException {
    String email = requestDTO.getEmail();
    checkExistingUser(email);
    Optional<PendingRegistration> registration = registrationRepository.findByEmail(email);
    if (registration.isPresent()) {
      return handleExistingRegistration(registration.get());
    } else return submitRegistration(email, userType);
  }

  @Override
  public VerifyUserResponseDTO verifyRegistration(VerifyRegistrationDTO verifyRegistrationDTO, String token) throws RuntimeException {
    String email = registerRedisService.getEmail(token);
    PendingRegistration pendingRegistration = getPendingRegistration(email);
    registerRedisService.verifyEmail(email, token);
    return createNewUser(pendingRegistration, verifyRegistrationDTO);
  }

  // helpers
  private void checkExistingUser(String email) throws DuplicateEntryException {
    Optional<User> user = userRepository.findByEmail(email);
    Optional<PendingRegistration> pendingUserOptional = registrationRepository.findByEmail(email);
    if (user.isPresent()) {
      throw new DuplicateEntryException("E-mail already existed! Enter a new e-mail or login");
    }
  }

  private InitialRegistrationResponseDTO handleExistingRegistration(PendingRegistration pendingRegistration) {
    Instant now = Instant.now();

    String email = pendingRegistration.getEmail();
    UserType userType = pendingRegistration.getUserType();

    // last pending registration is more than a day old
    if (now.minusSeconds(24 * 60 * 60).getEpochSecond() > pendingRegistration.getCreatedAt().getEpochSecond()) {
      registrationRepository.delete(pendingRegistration);
      return submitRegistration(email, userType);
    }

    // last verification email was sent less than an hour before
    if (now.minusSeconds(60 * 60).getEpochSecond() < pendingRegistration.getLastVerificationAttempt().getEpochSecond()) {
      return resendVerificationEmail(pendingRegistration);
    }
    return updateAndResendVerificationEmail(pendingRegistration);
  }

  public String generateVerificationToken() {
    return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
  }

  public InitialRegistrationResponseDTO submitRegistration(String email, UserType userType)
      throws DuplicateEntryException {
    PendingRegistration registration = new PendingRegistration();
    registration.setEmail(email);
    registration.setUserType(userType);
    registrationRepository.save(registration);

    String token = generateAndSaveRedisToken(email);

    String message = "Verification link has been sent to " + email + " for registration request as a " + userType +"."
                     + " Please check your e-mail and follow the next steps to verify your account!";

    return sendVerificationEmail(registration, token, message);
  }

  public InitialRegistrationResponseDTO resendVerificationEmail(PendingRegistration pendingRegistration) {
    String email = pendingRegistration.getEmail();
    String token = registerRedisService.getToken(email);

    // TODO : resend email func here

    String message = "Your last verification is still valid, we have resent it to your email at " + email;
    return registerResponse(message, token);
  }

  public InitialRegistrationResponseDTO updateAndResendVerificationEmail(PendingRegistration pendingRegistration) {
    pendingRegistration.setLastVerificationAttempt(Instant.now());
    registrationRepository.save(pendingRegistration);
    String email = pendingRegistration.getEmail();
    String token = generateAndSaveRedisToken(email);
    String message =
        "We have updated and resent your new verification link to your email at " + email;
    return sendVerificationEmail(pendingRegistration, token, message);
  }

  public InitialRegistrationResponseDTO sendVerificationEmail(PendingRegistration pendingRegistration, String token,
      String message) {
    String email = pendingRegistration.getEmail();
    UserType userType = pendingRegistration.getUserType();

    // TODO: implement sending email here
    // emailService.sendVerificationEmail(pendingRegistration.getEmail(), token/URL)


    return registerResponse(message, token);
  }

  public String generateAndSaveRedisToken(String email) {
    String token = generateVerificationToken();
    registerRedisService.saveVericationToken(email, token);
    return token;
  }

  public InitialRegistrationResponseDTO registerResponse(String message, String token) {
    InitialRegistrationResponseDTO responseDTO = new InitialRegistrationResponseDTO();
    responseDTO.setToken(token);
    responseDTO.setMessage(message);
    return responseDTO;
  }

  // Region - helpers for verification
  PendingRegistration getPendingRegistration(String email) throws RuntimeException {
    Optional<PendingRegistration> pendingRegistrationOptional = registrationRepository.findByEmail(email);
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
    User user = new User();
    user.setEmail(pendingRegistration.getEmail());
    user.setUserType(pendingRegistration.getUserType());
    user.setPasswordHash(passwordEncoder.encode(verifyRegistrationDTO.getPassword()));
    user.setFirstName(verifyRegistrationDTO.getFirstName());
    user.setLastName(verifyRegistrationDTO.getLastName());
    user.setPhoneNumber(verifyRegistrationDTO.getPhoneNumber());
    user.setIsVerified(true);
    userRepository.save(user);

    // ? delete or marked as verified?
    registrationRepository.delete(pendingRegistration);

    if (pendingRegistration.getUserType() == UserType.TENANT) {
      TenantInfo newLandlord = createNewLandlord(verifyRegistrationDTO, user);
      return new VerifyTenantResponseDTO(user, newLandlord);
    } else {
      return new VerifyUserResponseDTO(user);
    }
  }

  public TenantInfo createNewLandlord(VerifyRegistrationDTO verifyRegistrationDTO, User user) {
    TenantInfo newLandlord = new TenantInfo();
    newLandlord.setUser(user);
    newLandlord.setBusinessName(verifyRegistrationDTO.getBusinessName());
    newLandlord.setTaxId(verifyRegistrationDTO.getTaxId());
    tenantInfoRepository.save(newLandlord);
    return newLandlord;
  }

  public void checkPassword(String password, String confirmPassword) throws RuntimeException {
    if (!password.equals(confirmPassword)) {
      throw new PasswordDoesNotMatchException("confirmPassword field must be the same as password");
    }
  }
}
