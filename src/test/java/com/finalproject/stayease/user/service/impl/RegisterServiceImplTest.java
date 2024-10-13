package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.response.VerifyUserResponseDTO;
import com.finalproject.stayease.auth.service.RegisterRedisService;
import com.finalproject.stayease.exceptions.auth.PasswordDoesNotMatchException;
import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.exceptions.utils.DuplicateEntryException;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.service.PendingRegistrationService;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import com.finalproject.stayease.users.service.impl.RegisterServiceImpl;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Slf4j
public class RegisterServiceImplTest {

  @Mock
  private UsersService usersService;
  @Mock
  private TenantInfoService tenantInfoService;
  @Mock
  private PendingRegistrationService pendingRegistrationService;
  @Mock
  private RegisterRedisService registerRedisService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private MailService mailService;

  @InjectMocks
  private RegisterServiceImpl registerService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(registerService, "baseUrl", "http://localhost:8080");
    ReflectionTestUtils.setField(registerService, "apiVersion", "/api/v1");
    ReflectionTestUtils.setField(registerService, "feUrl", "http://localhost:3000");
  }

  @Test
  void initialRegistration_NewUser_Success() throws MessagingException, IOException {
    InitialRegistrationRequestDTO requestDTO = new InitialRegistrationRequestDTO();
    requestDTO.setEmail("test@example.com");
    UserType userType = UserType.USER;

    when(usersService.findByEmail(anyString())).thenReturn(Optional.empty());
    when(pendingRegistrationService.findByEmail(anyString())).thenReturn(Optional.empty());

    InitialRegistrationResponseDTO response = registerService.initialRegistration(requestDTO, userType);

    assertNotNull(response);
    assertTrue(response.getMessage().contains("Verification link has been sent"));
    verify(pendingRegistrationService).save(any(PendingRegistration.class));
    verify(registerRedisService).saveVericationToken(anyString(), anyString());
    verify(mailService).sendHtmlEmail(anyString(), eq("test@example.com"), anyString());
  }

  @Test
  void initialRegistration_ExistingUser_ThrowsException() {
    InitialRegistrationRequestDTO requestDTO = new InitialRegistrationRequestDTO();
    requestDTO.setEmail("existing@example.com");
    UserType userType = UserType.USER;

    when(usersService.findByEmail(anyString())).thenReturn(Optional.of(new Users()));

    assertThrows(DuplicateEntryException.class, () -> registerService.initialRegistration(requestDTO, userType));
  }

  @Test
  void verifyRegistration_Success() {
    String token = "validToken";
    String email = "test@example.com";
    VerifyRegistrationDTO verifyDTO = new VerifyRegistrationDTO();
    verifyDTO.setPassword("password");
    verifyDTO.setConfirmPassword("password");
    verifyDTO.setFirstName("John");
    verifyDTO.setLastName("Doe");

    PendingRegistration pendingRegistration = new PendingRegistration();
    pendingRegistration.setEmail(email);
    pendingRegistration.setUserType(UserType.USER);

    when(registerRedisService.getEmail(token)).thenReturn(email);
    when(pendingRegistrationService.findByEmail(email)).thenReturn(Optional.of(pendingRegistration));
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    VerifyUserResponseDTO response = registerService.verifyRegistration(verifyDTO, token);

    assertNotNull(response);
    assertEquals(email, response.getEmail());
    verify(usersService).save(any(Users.class));
    verify(pendingRegistrationService).deleteById(any());
  }

  @Test
  void verifyRegistration_PasswordMismatch_ThrowsException() {
    String token = "validToken";
    VerifyRegistrationDTO verifyDTO = new VerifyRegistrationDTO();
    verifyDTO.setPassword("password");
    verifyDTO.setConfirmPassword("differentPassword");

    when(registerRedisService.getEmail(token)).thenReturn("test@example.com");
    when(pendingRegistrationService.findByEmail(anyString())).thenReturn(Optional.of(new PendingRegistration()));

    assertThrows(PasswordDoesNotMatchException.class, () -> registerService.verifyRegistration(verifyDTO, token));
  }

  @Test
  void verifyRegistration_PendingRegistrationNotFound_ThrowsException() {
    String token = "validToken";
    VerifyRegistrationDTO verifyDTO = new VerifyRegistrationDTO();

    when(registerRedisService.getEmail(token)).thenReturn("test@example.com");
    when(pendingRegistrationService.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class, () -> registerService.verifyRegistration(verifyDTO, token));
  }
}
