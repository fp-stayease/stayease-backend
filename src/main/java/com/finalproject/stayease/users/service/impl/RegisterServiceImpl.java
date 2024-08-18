package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.auth.service.RegisterRedisService;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.users.repository.PendingRegistrationRepository;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.RegisterService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class RegisterServiceImpl implements RegisterService {

  private final UserRepository userRepository;
  private final PendingRegistrationRepository registrationRepository;
  private final RegisterRedisService registerRedisService;


  @Override
  @Transactional
  public InitialRegistrationResponseDTO initialRegistration(InitialRegistrationRequestDTO requestDTO, UserType userType) throws DuplicateEntryException {
    String email = requestDTO.getEmail();
    checkExistingUser(email);

    // TODO : create logic for reverification

    String token = generateVerificationToken();
    submitRegistration(requestDTO, userType, token);

    return registerResponse(email, userType, token);
  }

  @Override
  public VerifyUserResponseDTO verifyRegistration(VerifyRegistrationDTO verifyRegistrationDTO, String token) {
    String email = registerRedisService.getEmail(token);
    return null;
  }

  // helpers
  public void checkExistingUser(String email) throws DuplicateEntryException {
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      throw new DuplicateEntryException("E-mail already existed! Enter a new e-mail or login");
    }
  }

  public String generateVerificationToken() {
    return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
  }

  public void submitRegistration(InitialRegistrationRequestDTO requestDTO, UserType userType, String token) throws DuplicateEntryException {
    PendingRegistration registration = new PendingRegistration();
    registration.setEmail(requestDTO.getEmail());
    registration.setUserType(userType);
    registrationRepository.save(registration);

    registerRedisService.saveVericationToken(registration.getEmail(), token);

    // TODO : implement email verification send
    // * sendVerificationEmail()
  }

//  TODO: public void sendVerificationEmail()
  // change token generation to be here
  // create registerResponse here instead()

//  public void sendVerificationEmail(PendingRegistration pendingRegistration) {
//    String token = generateVerificationToken();
//    String verificationUrl = ""
//  }

  // TODO: resend email verification

  public InitialRegistrationResponseDTO registerResponse(String email, UserType userType, String token) {
    InitialRegistrationResponseDTO responseDTO = new InitialRegistrationResponseDTO();
    responseDTO.setToken(token);
    responseDTO.setMessage("Verification link has been sent to " + email + " for registration request as a " +  userType +
                           ". Please check your e-mail and follow the next steps to verify your account!");
    return responseDTO;
  }

  // Region - helpers for verification
  PendingRegistration getPendingRegistration(String email) {
    Optional<PendingRegistration> pendingRegistrationOptional = registrationRepository.findByEmail(email);
    if (pendingRegistrationOptional.isPresent()) {
      return pendingRegistrationOptional.get();
    } else throw new DataNotFoundException(email + " is not found as a pending registration or it may have expired. "
                                           + "Please submit a new registration request.");
  }
}
