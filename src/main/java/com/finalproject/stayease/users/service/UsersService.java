package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UsersService {
  Users save(Users user);
  Optional<Users> findByEmail(String email);
  Users getLoggedUser();

  void deleteUser(Users user);

  // Region - Helpers
  List<String> findAllAvatars();
  Optional<Users> findById(Long id);
  int hardDeleteStaleUsers(Instant timestamp);

  // Region - Quarantine
}
