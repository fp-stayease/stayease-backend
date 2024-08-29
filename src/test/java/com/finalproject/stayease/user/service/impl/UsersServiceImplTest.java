package com.finalproject.stayease.user.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.repository.UsersRepository;
import com.finalproject.stayease.users.service.impl.UsersServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class UsersServiceImplTest {

  @MockBean
  private UsersRepository usersRepository;

  @InjectMocks
  private UsersServiceImpl userService = new UsersServiceImpl(usersRepository);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userService = new UsersServiceImpl(usersRepository);
  }

  @Test
  public void findByEmailTest() {
    // Arrange
    Users user = new Users();
    user.setEmail("test@test.com");

    // Act
    Optional<Users> userOptional = userService.findByEmail("test@test.com");

    // Assert
    verify(usersRepository, times(1)).findByEmail(any());
  }
}
