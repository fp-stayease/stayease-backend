package com.finalproject.stayease;

import com.finalproject.stayease.config.EnvConfigProperties;
import com.finalproject.stayease.config.RsaKeyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({RsaKeyConfigProperties.class, EnvConfigProperties.class})
public class StayEaseApplication {

  public static void main(String[] args) {
    SpringApplication.run(StayEaseApplication.class, args);
  }

}
