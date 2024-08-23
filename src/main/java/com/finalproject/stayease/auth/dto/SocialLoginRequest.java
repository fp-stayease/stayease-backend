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
  private String firstName;
  private String lastName;
  private String pictureUrl;

  // Optional fields for tenant registration
  private String businessName;
  private String taxId;

  public SocialLoginRequest(String provider, String providerUserId, String email, String firstName, String lastName,
      String pictureUrl) {
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.pictureUrl = pictureUrl;
  }
}
