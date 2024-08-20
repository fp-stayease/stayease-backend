package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.UserService;
import java.util.Optional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Data
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }
}
