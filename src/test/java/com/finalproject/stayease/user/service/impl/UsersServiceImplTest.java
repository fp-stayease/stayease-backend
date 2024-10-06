package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.repository.UsersRepository;
import com.finalproject.stayease.users.service.impl.UsersServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UsersServiceImplTest {

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private UsersServiceImpl usersService;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void findByEmail_Success() {
    String email = "test@example.com";
    Users user = new Users();
    user.setEmail(email);

    when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Optional<Users> result = usersService.findByEmail(email);

    assertTrue(result.isPresent());
    assertEquals(email, result.get().getEmail());
  }

  @Test
  void getLoggedUser_Success() {
    String email = "logged@example.com";
    Users user = new Users();
    user.setEmail(email);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(email);
    when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Users result = usersService.getLoggedUser();

    assertNotNull(result);
    assertEquals(email, result.getEmail());
  }

  @Test
  void getLoggedUser_NotAuthenticated_ThrowsException() {
    when(securityContext.getAuthentication()).thenReturn(null);

    assertThrows(AccessDeniedException.class, () -> usersService.getLoggedUser());
  }

  @Test
  void getLoggedUser_UserNotFound_ThrowsException() {
    String email = "nonexistent@example.com";

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(email);
    when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(AccessDeniedException.class, () -> usersService.getLoggedUser());
  }

  @Test
  void deleteUser_Success() {
    Users user = new Users();
    usersService.deleteUser(user);

    assertNotNull(user.getDeletedAt());
    verify(usersRepository).save(user);
  }

  @Test
  void findAllAvatars_Success() {
    List<String> avatars = Arrays.asList("avatar1.jpg", "avatar2.jpg");
    when(usersRepository.findAllAvatars()).thenReturn(avatars);

    List<String> result = usersService.findAllAvatars();

    assertEquals(avatars, result);
  }

  @Test
  void findById_Success() {
    Long id = 1L;
    Users user = new Users();
    user.setId(id);

    when(usersRepository.findById(id)).thenReturn(Optional.of(user));

    Optional<Users> result = usersService.findById(id);

    assertTrue(result.isPresent());
    assertEquals(id, result.get().getId());
  }

  @Test
  void hardDeleteStaleUsers_Success() {
    Instant timestamp = Instant.now();
    int deletedCount = 5;

    when(usersRepository.hardDeleteStaleUsers(timestamp)).thenReturn(deletedCount);

    int result = usersService.hardDeleteStaleUsers(timestamp);

    assertEquals(deletedCount, result);
  }

  @Test
  void save_Success() {
    Users user = new Users();
    user.setEmail("new@example.com");

    when(usersRepository.save(user)).thenReturn(user);

    Users result = usersService.save(user);

    assertNotNull(result);
    assertEquals(user.getEmail(), result.getEmail());
    verify(usersRepository).save(user);
  }
}
