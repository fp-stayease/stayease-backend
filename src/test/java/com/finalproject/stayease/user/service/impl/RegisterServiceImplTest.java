package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.auth.service.RegisterRedisService;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.users.repository.PendingRegistrationRepository;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.impl.RegisterServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class RegisterServiceImplTest {

  @MockBean
  private UserRepository userRepository;
  @MockBean
  private TenantInfoRepository tenantInfoRepository;
  @MockBean
  private PendingRegistrationRepository registrationRepository;
  @MockBean
  private RegisterRedisService registerRedisService;

  @InjectMocks
  private RegisterServiceImpl registerService = new RegisterServiceImpl(userRepository, tenantInfoRepository,
      registrationRepository,
      registerRedisService);
  @Autowired
  private PendingRegistrationRepository pendingRegistrationRepository;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    registerService = new RegisterServiceImpl(userRepository, tenantInfoRepository, registrationRepository,
        registerRedisService);
  }

  @Test
  void initialRegistrationTest() {
    // Arrange
    String email = "email@email.com";
    UserType userType = UserType.USER;

    InitialRegistrationRequestDTO requestDTO = new InitialRegistrationRequestDTO();
    requestDTO.setEmail(email);

    PendingRegistration pendingUser = new PendingRegistration();
    pendingUser.setId(1L);
    pendingUser.setEmail(email);
    pendingUser.setUserType(userType);

    // Act
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(registrationRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(registrationRepository.save(any())).thenReturn(pendingUser);
    InitialRegistrationResponseDTO responseDTO = registerService.initialRegistration(requestDTO, userType);

    //Assert
    assertNotNull(responseDTO);
    verify(userRepository, times(1)).findByEmail(any(String.class));
    verify(registrationRepository, times(1)).save(any(PendingRegistration.class));
    verify(registerRedisService, times(1)).saveVericationToken(any(), any());
    assertNull(pendingUser.getVerifiedAt());
    assert(pendingUser.getUserType()).equals(UserType.USER);
    assert(responseDTO.getMessage()).startsWith("Verification link has been sent to ");
  }

  @Test
  void initialRegistrationTest_duplicateEmail() {
    // Arrange
    String email = "email@email.com";
    UserType userType = UserType.USER;

    InitialRegistrationRequestDTO requestDTO  = new InitialRegistrationRequestDTO();
    requestDTO.setEmail(email);

    User existingUser = new User();
    existingUser.setId(1L);
    existingUser.setEmail(email);

    // Act
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // Act & Assert
    assertThrows(DuplicateEntryException.class, () -> registerService.initialRegistration(requestDTO, userType));
  }

  @Test
  void verifyRegistrationTest() {
    // Arrange
    VerifyRegistrationDTO verifyDTO = new VerifyRegistrationDTO();
    String token = "token";
    verifyDTO.setPassword("password");
    verifyDTO.setConfirmPassword("password");
    verifyDTO.setFirstName("John");
    verifyDTO.setLastName("Doe");
    verifyDTO.setPhoneNumber("123456789");

    String email = "email@email.com";
    PendingRegistration pendingUser = new PendingRegistration();
    pendingUser.setId(1L);
    pendingUser.setEmail(email);
    pendingUser.setUserType(UserType.USER);

    User newUser = new User();
    newUser.setId(1L);
    newUser.setEmail(email);
//    newUser.setUserType(UserType.USER);
//    newUser.setPasswordHash("password");
//    newUser.setFirstName("John");
//    newUser.setLastName("Doe");
//    newUser.setPhoneNumber("123456789");
    newUser.setIsVerified(true);

    // Act
    when(registerRedisService.getEmail(token)).thenReturn(email);
    when(pendingRegistrationRepository.findByEmail(email)).thenReturn(Optional.of(pendingUser));
    VerifyUserResponseDTO responseDTO = registerService.verifyRegistration(verifyDTO, token);

    // Assert
    assertNotNull(responseDTO);
    verify(pendingRegistrationRepository, times(1)).findByEmail(email);
    verify(userRepository, times(1)).save(any(User.class));
    assertEquals(newUser.getEmail(), email);
  }
}
