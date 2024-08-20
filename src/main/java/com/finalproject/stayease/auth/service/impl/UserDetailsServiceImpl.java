package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.entity.UserAuth;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.UserService;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Data
public class UserDetailsServiceImpl implements UserDetailsService {

  public final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UserAuth user = userService
        .findByEmail(email)
        .map(UserAuth::new)
        .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
    return new UserAuth(user);
  }
}
