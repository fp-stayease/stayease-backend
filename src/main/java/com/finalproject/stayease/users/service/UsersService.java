package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import java.util.List;
import java.util.Optional;

public interface UsersService {
  Users save(Users user);
  Optional<Users> findByEmail(String email);
  Users getLoggedUser();

  // Region - Helpers
  List<String> findAllAvatars();

  // Region - Quarantine
  Optional<Users> findById(Long id);
}
