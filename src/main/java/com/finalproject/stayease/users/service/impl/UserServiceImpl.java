package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.dto.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.UserService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;


  @Override
  public InitialRegistrationResponseDTO initialRegistration(String email, String role) {
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      throw new DuplicateEntryException("E-mail already existed! Enter a new e-mail or login");
    }
    String token = generateVerificationToken();
    User newUser = new User();
    newUser.setEmail(email);
    newUser.setUserType(role);
    newUser.setIsVerified(false);
    userRepository.save(newUser);

    InitialRegistrationResponseDTO responseDTO = new InitialRegistrationResponseDTO();
    responseDTO.setToken(token);
    responseDTO.setMessage("Verification link has been sent to " + email + " for registration request as a " + role +
                           ". Please check your e-mail and follow the next steps to verify your account!");
    return responseDTO;
  }

  public String generateVerificationToken() {
    return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
  }
}
