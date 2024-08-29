package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
import com.finalproject.stayease.users.repository.UsersRepository;
import com.finalproject.stayease.users.service.TenantInfoService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.Data;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Data
@Service
@Transactional
public class UsersServiceImpl implements UsersService {

  private final UsersRepository usersRepository;

  @Override
  public Optional<Users> findByEmail(String email) {
    return usersRepository.findByEmail(email);
  }

  @Override
  public Users getLoggedUser() throws RuntimeException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new AccessDeniedException("You must be logged in to access this resource");
    }
    String email = authentication.getName();
    return findByEmail(email).orElseThrow(() -> new AccessDeniedException("You must be logged in to access this resource"));
  }

  @Override
  public Optional<Users> findById(Long id) {
    return usersRepository.findById(id);
  }

  @Override
  public void save(Users user) {
    usersRepository.save(user);
  }
}
