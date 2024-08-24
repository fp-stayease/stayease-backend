package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.entity.UserAuth;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.service.UserService;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

  public final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    Optional<User> userOpt = userService.findByEmail(email);
    if (userOpt.isEmpty()) {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }
    User user = userOpt.get();
    log.info("Found user: {}", user.getEmail());
    log.info("User password hash: {}", user.getPasswordHash());

    return new UserAuth(user);
  }
}
