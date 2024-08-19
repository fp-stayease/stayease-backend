package com.finalproject.stayease.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginRequest {
  @NotEmpty(message = "Please provide a valid provider")
  private String provider;
  @NotEmpty(message = "Please provide a valid provider user ID")
  private String providerUserId;
  @Email(message = "Please enter a valid e-mail.")
  private String email;
  @NotEmpty
  private String firstName;
  @NotEmpty
  private String lastName;

  // Optional fields for tenant registration
  private String businessName;
  private String taxId;

  public SocialLoginRequest(String provider, String providerUserId, String email, String firstName, String lastName) {
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
