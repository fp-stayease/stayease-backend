package com.finalproject.stayease.auth.model.dto.register.init;

import lombok.Data;

@Data
public class InitialRegistrationResponseDTO {

  private String message;
  private String verificationToken;

}
