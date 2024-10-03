package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.auth.repository.AuthRedisRepository;
import com.finalproject.stayease.exceptions.users.UserNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.exceptions.utils.InvalidTokenException;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import com.finalproject.stayease.users.repository.EmailChangeRedisRepository;
import com.finalproject.stayease.users.service.UsersService;
import com.finalproject.stayease.users.service.impl.EmailChangeServiceImpl;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailChangeServiceImplTest {

  @Mock
  private EmailChangeRedisRepository emailChangeRedisRepository;
  @Mock
  private MailService mailService;
  @Mock
  private JwtEncoder jwtEncoder;
  @Mock
  private JwtDecoder jwtDecoder;
  @Mock
  private UsersService usersService;
  @Mock
  private AuthRedisRepository authRedisRepository;

  @InjectMocks
  private EmailChangeServiceImpl emailChangeService;

  @Value("${FE_URL}")
  private String FE_URL;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailChangeService, "feUrl", FE_URL);
    ReflectionTestUtils.setField(emailChangeService, "TOKEN_EXPIRATION_HOURS", 1);
  }

  @Test
  void requestEmailChange_Success() throws MessagingException, IOException {
    Users user = new Users();
    user.setEmail("old@example.com");
    user.setSocialLogins(new HashSet<>());

    RequestEmailChangeDTO requestDTO = new RequestEmailChangeDTO();
    requestDTO.setNewEmail("new@example.com");

    when(usersService.findByEmail(requestDTO.getNewEmail())).thenReturn(Optional.empty());

    Jwt mockJwt = mock(Jwt.class);
    when(mockJwt.getTokenValue()).thenReturn("mocked-jwt-token");
    when(jwtEncoder.encode(any())).thenReturn(mockJwt);

    String result = emailChangeService.requestEmailChange(user, requestDTO);

    assertNotNull(result);
    assertTrue(result.startsWith(FE_URL + "/profile/settings/verify-email?token="));
    verify(emailChangeRedisRepository).saveToken(anyString(), anyString());
    verify(mailService).sendHtmlEmail(anyString(), eq("new@example.com"), anyString());
  }

  @Test
  void requestEmailChange_SocialLoginUser_ThrowsException() {
    Users user = new Users();
    user.setEmail("old@example.com");
    user.setSocialLogins(new HashSet<>());
    user.getSocialLogins().add(new SocialLogin()); // Add a social login

    RequestEmailChangeDTO requestDTO = new RequestEmailChangeDTO();
    requestDTO.setNewEmail("new@example.com");

    assertThrows(InvalidRequestException.class, () -> emailChangeService.requestEmailChange(user, requestDTO));
  }

  @Test
  void requestEmailChange_EmailAlreadyInUse_ThrowsException() {
    Users user = new Users();
    user.setEmail("old@example.com");
    user.setSocialLogins(new HashSet<>());

    RequestEmailChangeDTO requestDTO = new RequestEmailChangeDTO();
    requestDTO.setNewEmail("existing@example.com");

    when(usersService.findByEmail("existing@example.com")).thenReturn(Optional.of(new Users()));

    assertThrows(InvalidRequestException.class, () -> emailChangeService.requestEmailChange(user, requestDTO));
  }

  @Test
  void verifyEmailChange_Success() {
    String tokenUUID = "validToken";
    String emailChangeJwt = "validJwt";
    Users user = new Users();
    user.setEmail("old@example.com");

    when(emailChangeRedisRepository.isValid(tokenUUID)).thenReturn(true);
    when(emailChangeRedisRepository.getJwt(tokenUUID)).thenReturn(emailChangeJwt);

    Jwt mockJwt = mock(Jwt.class);
    when(mockJwt.getSubject()).thenReturn("old@example.com");
    when(mockJwt.getClaim("newEmail")).thenReturn("new@example.com");
    when(jwtDecoder.decode(emailChangeJwt)).thenReturn(mockJwt);

    when(usersService.findByEmail("old@example.com")).thenReturn(Optional.of(user));
    when(usersService.save(user)).thenReturn(user);

    Users result = emailChangeService.verifyEmailChange(tokenUUID);

    assertEquals("new@example.com", result.getEmail());
    verify(emailChangeRedisRepository).verifyEmail(tokenUUID);
    verify(authRedisRepository).blacklistKey("old@example.com");
  }

  @Test
  void verifyEmailChange_InvalidToken_ThrowsException() {
    String tokenUUID = "invalidToken";
    when(emailChangeRedisRepository.isValid(tokenUUID)).thenReturn(false);

    assertThrows(InvalidTokenException.class, () -> emailChangeService.verifyEmailChange(tokenUUID));
  }

  @Test
  void verifyEmailChange_UserNotFound_ThrowsException() {
    String tokenUUID = "validToken";
    String emailChangeJwt = "validJwt";

    when(emailChangeRedisRepository.isValid(tokenUUID)).thenReturn(true);
    when(emailChangeRedisRepository.getJwt(tokenUUID)).thenReturn(emailChangeJwt);

    Jwt mockJwt = mock(Jwt.class);
    when(mockJwt.getSubject()).thenReturn("old@example.com");
    when(mockJwt.getClaim("newEmail")).thenReturn("new@example.com");
    when(jwtDecoder.decode(emailChangeJwt)).thenReturn(mockJwt);

    when(usersService.findByEmail("old@example.com")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> emailChangeService.verifyEmailChange(tokenUUID));
  }
}
