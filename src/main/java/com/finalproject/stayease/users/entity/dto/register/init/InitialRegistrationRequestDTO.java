package com.finalproject.stayease.users.entity.dto.register.init;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class InitialRegistrationRequestDTO {

  @NotEmpty
  private String email;

}
