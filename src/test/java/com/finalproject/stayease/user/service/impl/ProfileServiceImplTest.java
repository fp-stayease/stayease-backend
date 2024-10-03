package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.users.TenantInfoNotFoundException;
import com.finalproject.stayease.exceptions.users.UserNotFoundException;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.UpdateTenantInfoRequestDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import com.finalproject.stayease.users.service.impl.ProfileServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

  @Mock
  private UsersService usersService;

  @Mock
  private TenantInfoService tenantInfoService;

  @InjectMocks
  private ProfileServiceImpl profileService;

  @Test
  void updateProfile_Success() {
    Users user = new Users();
    user.setEmail("test@example.com");

    UpdateUserProfileRequestDTO requestDTO = new UpdateUserProfileRequestDTO();
    requestDTO.setFirstName("John");
    requestDTO.setLastName("Doe");
    requestDTO.setPhoneNumber("1234567890");

    when(usersService.save(user)).thenReturn(user);

    Users result = profileService.updateProfile(user, requestDTO);

    assertEquals("John", result.getFirstName());
    assertEquals("Doe", result.getLastName());
    assertEquals("1234567890", result.getPhoneNumber());
    verify(usersService).save(user);
  }

  @Test
  void updateTenantInfo_Success() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);
    TenantInfo tenantInfo = new TenantInfo();
    tenant.setTenantInfo(tenantInfo);

    UpdateTenantInfoRequestDTO requestDTO = new UpdateTenantInfoRequestDTO();
    requestDTO.setBusinessName("New Business");
    requestDTO.setTaxId("123456789");

    when(tenantInfoService.findByTenant(tenant)).thenReturn(Optional.of(tenantInfo));
    when(tenantInfoService.save(tenantInfo)).thenReturn(tenantInfo);

    Users result = profileService.updateTenantInfo(tenant, requestDTO);

    assertEquals("New Business", result.getTenantInfo().getBusinessName());
    assertEquals("123456789", result.getTenantInfo().getTaxId());
    verify(tenantInfoService).save(tenantInfo);
  }

  @Test
  void updateTenantInfo_NotTenant_ThrowsException() {
    Users user = new Users();
    user.setUserType(Users.UserType.USER);

    UpdateTenantInfoRequestDTO requestDTO = new UpdateTenantInfoRequestDTO();

    assertThrows(UserNotFoundException.class, () -> profileService.updateTenantInfo(user, requestDTO));
  }

  @Test
  void updateTenantInfo_TenantInfoNotFound_ThrowsException() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    UpdateTenantInfoRequestDTO requestDTO = new UpdateTenantInfoRequestDTO();

    when(tenantInfoService.findByTenant(tenant)).thenReturn(Optional.empty());

    assertThrows(TenantInfoNotFoundException.class, () -> profileService.updateTenantInfo(tenant, requestDTO));
  }

  @Test
  void changeAvatar_Success() {
    Users user = new Users();
    String imageUrl = "http://example.com/avatar.jpg";

    when(usersService.save(user)).thenReturn(user);

    Users result = profileService.changeAvatar(user, imageUrl);

    assertEquals(imageUrl, result.getAvatar());
    verify(usersService).save(user);
  }

  @Test
  void removeAvatar_Success() {
    Users user = new Users();
    user.setAvatar("http://example.com/avatar.jpg");

    when(usersService.save(user)).thenReturn(user);

    Users result = profileService.removeAvatar(user);

    assertNull(result.getAvatar());
    verify(usersService).save(user);
  }
}
