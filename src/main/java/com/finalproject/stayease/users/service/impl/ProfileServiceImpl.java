package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import com.finalproject.stayease.users.service.ProfileService;
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
  public Users changeAvatar(Users user, String imageUrl) {
    user.setAvatar(imageUrl);
    return usersService.save(user);
  }

  @Override
  public Users removeAvatar(Users user) {
    user.setAvatar(null);
    return usersService.save(user);
  }
}
