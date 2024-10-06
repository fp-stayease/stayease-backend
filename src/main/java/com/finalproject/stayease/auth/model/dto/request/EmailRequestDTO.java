package com.finalproject.stayease.auth.model.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailRequestDTO {

  @Email
  private String email;

}
