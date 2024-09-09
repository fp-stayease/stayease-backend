package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface UsersService {
  void save(Users user);
  Optional<Users> findByEmail(String email);
  Users getLoggedUser();

  // Region - Profile Showing



  // Region - Quarantine
  Optional<Users> findById(Long id);
}
