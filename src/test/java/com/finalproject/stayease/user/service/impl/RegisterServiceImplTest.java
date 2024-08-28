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
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.users.service.PendingRegistrationService;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import com.finalproject.stayease.users.service.impl.RegisterServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class RegisterServiceImplTest {

  @MockBean
  private UsersService usersService;
  @MockBean
  private TenantInfoService tenantInfoService;
  @MockBean
  private PendingRegistrationService pendingRegistrationService;
  @MockBean
  private RegisterRedisService registerRedisService;
  @MockBean
  private PasswordEncoder passwordEncoder;
  @MockBean
  private MailService mailService;

  @InjectMocks
  private RegisterServiceImpl registerService = new RegisterServiceImpl(usersService, tenantInfoService,
      pendingRegistrationService,
      registerRedisService, passwordEncoder, mailService);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    registerService = new RegisterServiceImpl(usersService, tenantInfoService,
        pendingRegistrationService,
        registerRedisService, passwordEncoder, mailService);
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
    when(usersService.findByEmail(email)).thenReturn(Optional.empty());
    when(pendingRegistrationService.findByEmail(email)).thenReturn(Optional.empty());
    InitialRegistrationResponseDTO responseDTO = registerService.initialRegistration(requestDTO, userType);

    //Assert
    assertNotNull(responseDTO);
    verify(usersService, times(1)).findByEmail(any(String.class));
    verify(pendingRegistrationService, times(1)).save(any(PendingRegistration.class));
    verify(registerRedisService, times(1)).saveVericationToken(any(), any());
    assertNull(pendingUser.getVerifiedAt());
    assert (pendingUser.getUserType()).equals(UserType.USER);
    assert (responseDTO.getMessage()).startsWith("Verification link has been sent to ");
  }

  @Test
  void initialRegistrationTest_duplicateEmail() {
    // Arrange
    String email = "email@email.com";
    UserType userType = UserType.USER;

    InitialRegistrationRequestDTO requestDTO = new InitialRegistrationRequestDTO();
    requestDTO.setEmail(email);

    Users existingUser = new Users();
    existingUser.setId(1L);
    existingUser.setEmail(email);

    // Act
    when(usersService.findByEmail(email)).thenReturn(Optional.of(existingUser));

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

    Users newUser = new Users();
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
    when(pendingRegistrationService.findByEmail(email)).thenReturn(Optional.of(pendingUser));
    VerifyUserResponseDTO responseDTO = registerService.verifyRegistration(verifyDTO, token);

    // Assert
    assertNotNull(responseDTO);
    verify(pendingRegistrationService, times(1)).findByEmail(email);
    verify(usersService, times(1)).save(any(Users.class));
    assertEquals(newUser.getEmail(), email);
  }
}
