package com.finalproject.stayease.user.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.finalproject.stayease.users.entity.User;
import com.finalproject.stayease.users.repository.SocialLoginRepository;
import com.finalproject.stayease.users.repository.TenantInfoRepository;
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
  @MockBean
  private SocialLoginRepository socialLoginRepository;
  @MockBean
  private TenantInfoRepository tenantInfoRepository;

  @InjectMocks
  private UserServiceImpl userService = new UserServiceImpl(userRepository, socialLoginRepository, tenantInfoRepository);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    userService = new UserServiceImpl(userRepository, socialLoginRepository, tenantInfoRepository);
  }

  @Test
  public void findByEmailTest() {
    // Arrange
    User user = new User();
    user.setEmail("test@test.com");

    // Act
    Optional<User> userOptional = userService.findByEmail("test@test.com");

    // Assert
    verify(userRepository, times(1)).findByEmail(any());
  }
}
