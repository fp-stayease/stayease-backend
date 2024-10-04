package com.finalproject.stayease.users.service.impl;


import com.finalproject.stayease.exceptions.users.TenantInfoNotFoundException;
import com.finalproject.stayease.exceptions.users.UserNotFoundException;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.UpdateTenantInfoRequestDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import com.finalproject.stayease.users.service.ProfileService;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Data
@Slf4j
public class ProfileServiceImpl implements ProfileService {

  private final UsersService usersService;
  private final TenantInfoService tenantInfoService;


  @Override
  public Users updateProfile(Users user, UpdateUserProfileRequestDTO requestDTO) {
    log.info("Updating user profile: " + requestDTO);
    Optional.ofNullable(requestDTO.getFirstName()).ifPresent(user::setFirstName);
    Optional.ofNullable(requestDTO.getLastName()).ifPresent(user::setLastName);
    Optional.ofNullable(requestDTO.getPhoneNumber()).ifPresent(user::setPhoneNumber);
    Optional.ofNullable(requestDTO.getAvatarUrl()).ifPresent(user::setAvatar);
    return usersService.save(user);
  }

  @Override
  public Users updateTenantInfo(Users tenant, UpdateTenantInfoRequestDTO requestDTO) {
    TenantInfo tenantInfo = validateTenant(tenant);
    log.info("Updating tenant info: " + requestDTO);
    Optional.ofNullable(requestDTO.getBusinessName()).ifPresent(tenantInfo::setBusinessName);
    Optional.ofNullable(requestDTO.getTaxId()).ifPresent(tenantInfo::setTaxId);
    tenantInfoService.save(tenantInfo);
    return tenant;
  }

  @Override
  public Users changeAvatar(Users user, String imageUrl) {
    user.setAvatar(imageUrl);
    log.info("Updating user avatar: " + imageUrl);
    return usersService.save(user);
  }

  @Override
  public Users removeAvatar(Users user) {
    user.setAvatar(null);
    log.info("Removing user avatar");
    return usersService.save(user);
  }

  private TenantInfo validateTenant(Users tenant) {
    if (tenant.getUserType() != Users.UserType.TENANT) {
      throw new UserNotFoundException("User is not a tenant");
    }
    return tenantInfoService.findByTenant(tenant).orElseThrow(
        () -> new TenantInfoNotFoundException("Tenant info not found for tenant: " + tenant.getEmail()));
  }
}
