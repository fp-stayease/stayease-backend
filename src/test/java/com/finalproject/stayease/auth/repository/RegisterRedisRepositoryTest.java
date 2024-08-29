package com.finalproject.stayease.auth.repository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RegisterRedisRepositoryTest {

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private RegisterRedisRepository registerRedisRepository = new RegisterRedisRepository(redisTemplate);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    registerRedisRepository  = new RegisterRedisRepository(redisTemplate);
  }

  @Test
  void testSaveVerificationToken() {
    String email = "test@example.com";
    String token = "sampleToken";

    registerRedisRepository.saveVerificationToken(email, token);

    verify(valueOperations).set("stayease:verification:strings:" + email, token, 1, TimeUnit.HOURS);
  }
}
