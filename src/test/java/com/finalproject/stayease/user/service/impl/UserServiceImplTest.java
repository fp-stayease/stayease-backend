package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.dto.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.impl.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class UserServiceImplTest {

  @MockBean
  private UserRepository userRepository;

  @InjectMocks
  private UserServiceImpl userService = new UserServiceImpl(userRepository);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userService = new UserServiceImpl(userRepository);
  }

  @Test
  void initialRegistrationTest() {
    // Arrange
    String email = "email@email.com";
    String role = "USER";

    User newUser = new User();
    newUser.setId(1L);
    newUser.setEmail(email);
    newUser.setUserType(role);
    newUser.setIsVerified(false);

    // Act
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(newUser);
    InitialRegistrationResponseDTO responseDTO = userService.initialRegistration(email, role);

    //Assert
    assertNotNull(responseDTO);
    verify(userRepository, times(1)).findByEmail(any(String.class));
    verify(userRepository, times(1)).save(any(User.class));
    assertFalse(newUser.getIsVerified());
    assert(newUser.getUserType()).equals("USER");
    assert(responseDTO.getMessage()).startsWith("Verification link has been sent to ");
  }

  @Test
  void initialRegistrationTest_duplicateEmail() {
    // Arrange
    String email = "email@email.com";
    String role = "USER";

    User existingUser = new User();
    existingUser.setId(1L);
    existingUser.setEmail(email);

    // Act
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // Act & Assert
    assertThrows(DuplicateEntryException.class, () -> userService.initialRegistration(email, role));
  }
}
