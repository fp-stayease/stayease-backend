package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface UsersService {
  void save(Users user);
  Optional<Users> findByEmail(String email);
  Users getLoggedUser();

  // Region - Profile Related
  Users updateProfile(Users user, UpdateUserProfileRequestDTO requestDTO);

  void changeAvatar(String imageUrl);
  void removeAvatar();

  String requestEmailChange(Users user, RequestEmailChangeDTO requestDTO) throws MessagingException,
      IOException;

  void verifyEmailChange(String tokenUUID);

  // Region - Helpers
  List<String> findAllAvatars();

  // Region - Quarantine
  Optional<Users> findById(Long id);
}
