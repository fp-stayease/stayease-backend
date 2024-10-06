package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.auth.model.dto.SocialLoginRequest;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.repository.SocialLoginRepository;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import com.finalproject.stayease.users.service.impl.SocialLoginServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SocialLoginServiceImplTest {

  @Mock
  private SocialLoginRepository socialLoginRepository;
  @Mock
  private UsersService usersService;
  @Mock
  private TenantInfoService tenantInfoService;

  @InjectMocks
  private SocialLoginServiceImpl socialLoginService;

  @Test
  void registerOAuth2User_User_Success() {
    SocialLoginRequest request = new SocialLoginRequest();
    request.setEmail("test@example.com");
    request.setUserType(UserType.USER);
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setProvider("google");
    request.setProviderUserId("123456");

    when(usersService.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(socialLoginRepository.save(any(SocialLogin.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Users result = socialLoginService.registerOAuth2User(request);

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    assertEquals(UserType.USER, result.getUserType());
    verify(usersService).save(any(Users.class));
    verify(socialLoginRepository).save(any(SocialLogin.class));
    verify(tenantInfoService, never()).save(any(TenantInfo.class));
  }

  @Test
  void registerOAuth2User_Tenant_Success() {
    SocialLoginRequest request = new SocialLoginRequest();
    request.setEmail("tenant@example.com");
    request.setUserType(UserType.TENANT);
    request.setFirstName("Jane");
    request.setLastName("Doe");
    request.setProvider("facebook");
    request.setProviderUserId("789012");
    request.setBusinessName("Jane's Business");

    when(usersService.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(socialLoginRepository.save(any(SocialLogin.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(tenantInfoService.save(any(TenantInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Users result = socialLoginService.registerOAuth2User(request);

    assertNotNull(result);
    assertEquals("tenant@example.com", result.getEmail());
    assertEquals(UserType.TENANT, result.getUserType());
    verify(usersService).save(any(Users.class));
    verify(socialLoginRepository).save(any(SocialLogin.class));
    verify(tenantInfoService).save(any(TenantInfo.class));
  }

  @Test
  void findByUser_Success() {
    Users user = new Users();
    SocialLogin socialLogin = new SocialLogin();
    socialLogin.setUser(user);

    when(socialLoginRepository.findByUser(user)).thenReturn(Optional.of(socialLogin));

    Optional<SocialLogin> result = socialLoginService.findByUser(user);

    assertTrue(result.isPresent());
    assertEquals(socialLogin, result.get());
  }
}
