package com.finalproject.stayease.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.auth.service.RegisterRedisService;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.users.entity.PendingRegistration;
import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.entity.dto.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.repository.PendingRegistrationRepository;
import com.finalproject.stayease.users.repository.UserRepository;
import com.finalproject.stayease.users.service.impl.RegisterServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class RegisterServiceImplTest {

  @MockBean
  private UserRepository userRepository;
  @MockBean
  private PendingRegistrationRepository registrationRepository;
  @MockBean
  private RegisterRedisService registerRedisService;

  @InjectMocks
  private RegisterServiceImpl userService = new RegisterServiceImpl(userRepository, registrationRepository, registerRedisService);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userService = new RegisterServiceImpl(userRepository, registrationRepository, registerRedisService);
  }

  @Test
  void initialRegistrationTest() {
    // Arrange
    String email = "email@email.com";
    UserType userType = UserType.USER;

    InitialRegistrationRequestDTO requestDTO = new InitialRegistrationRequestDTO();
    requestDTO.setEmail(email);

    PendingRegistration pendingUser = new PendingRegistration();
    pendingUser.setId(1L);
    pendingUser.setEmail(email);
    pendingUser.setUserType(userType);

    // Act
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(registrationRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(registrationRepository.save(any())).thenReturn(pendingUser);
    InitialRegistrationResponseDTO responseDTO = userService.initialRegistration(requestDTO, userType);

    //Assert
    assertNotNull(responseDTO);
    verify(userRepository, times(1)).findByEmail(any(String.class));
    verify(registrationRepository, times(1)).save(any(PendingRegistration.class));
    verify(registerRedisService, times(1)).saveVericationToken(any(), any());
    assertNull(pendingUser.getVerifiedAt());
    assert(pendingUser.getUserType()).equals(UserType.USER);
    assert(responseDTO.getMessage()).startsWith("Verification link has been sent to ");
  }

  @Test
  void initialRegistrationTest_duplicateEmail() {
    // Arrange
    String email = "email@email.com";
    UserType userType = UserType.USER;

    InitialRegistrationRequestDTO requestDTO  = new InitialRegistrationRequestDTO();
    requestDTO.setEmail(email);

    User existingUser = new User();
    existingUser.setId(1L);
    existingUser.setEmail(email);

    // Act
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    // Act & Assert
    assertThrows(DuplicateEntryException.class, () -> userService.initialRegistration(requestDTO, userType));
  }
}
