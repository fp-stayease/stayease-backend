package com.finalproject.stayease.users.entity.dto.register.verify.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class VerifyRegistrationDTO {

  @NotEmpty(message = "Password is required!")
  private String password;

  @NotEmpty(message = "Please confirm password!")
  private String confirmPassword;

  @NotEmpty(message = "Please enter your first name.")
  private String firstName;

  @NotEmpty
  private String lastName;

  private String phoneNumber;

  // for landlords
  private String businessName;
  private String taxId;
}
