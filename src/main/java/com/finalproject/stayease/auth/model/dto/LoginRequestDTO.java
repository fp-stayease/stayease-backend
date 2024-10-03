package com.finalproject.stayease.auth.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequestDTO {

  @NotEmpty
  private String email;

  @NotEmpty
  private String password;
}
