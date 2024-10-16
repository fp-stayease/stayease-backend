package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.UpdateTenantInfoRequestDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;

public interface ProfileService {
  Users updateProfile(Users user, UpdateUserProfileRequestDTO requestDTO);
  Users updateTenantInfo(Users tenant, UpdateTenantInfoRequestDTO requestDTO);
  Users changeAvatar(Users user, String imageUrl);
  Users removeAvatar(Users user);
}
