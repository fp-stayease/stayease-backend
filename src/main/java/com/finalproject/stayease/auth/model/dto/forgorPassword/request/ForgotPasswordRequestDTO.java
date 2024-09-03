package com.finalproject.stayease.auth.model.dto.forgorPassword.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {

  @NotEmpty(message = "Please enter email")
  private String email;

}
