package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;

public interface UsersService {
  void save(Users user);
  Optional<Users> findByEmail(String email);
  Users getLoggedUser();


  // Region - Quarantine
  Optional<Users> findById(Long id);
}
