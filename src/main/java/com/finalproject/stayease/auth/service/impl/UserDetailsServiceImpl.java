package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.entity.UserAuth;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
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
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

  public final UsersService usersService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    Optional<Users> userOpt = usersService.findByEmail(email);
    if (userOpt.isEmpty()) {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }
    Users user = userOpt.get();
    log.info("Found user: {}", user.getEmail());

    return new UserAuth(user);
  }
}
