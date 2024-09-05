package com.finalproject.stayease.auth.model.dto.forgorPassword.reset;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {

  @NotEmpty(message = "Please enter password")
  private String password;

  @NotEmpty(message = "Please enter to confirm password")
  private String confirmPassword;

}
