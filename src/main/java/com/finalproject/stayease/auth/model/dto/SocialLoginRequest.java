package com.finalproject.stayease.auth.model.dto;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SocialLoginRequest {
  @NotEmpty(message = "Please provide a valid provider")
  private String provider;
  @NotEmpty(message = "Please provide a valid provider user ID")
  private String providerUserId;
  @Email(message = "Please enter a valid e-mail.")
  private String email;
  @NotEmpty(message = "Must be chosen")
  private Users.UserType userType;
  private String firstName;
  private String lastName;
  private String avatar;

  // Optional fields for tenant registration
  private String businessName;
  private String taxId;

  public SocialLoginRequest(String provider, String providerUserId, String email, UserType userType, String firstName,
      String lastName,
      String avatar) {
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.email = email;
    this.userType = userType;
    this.firstName = firstName;
    this.lastName = lastName;
    this.avatar= avatar;
  }

  // for tenants
  public SocialLoginRequest(String provider, String providerUserId, String email, UserType userType, String firstName,
      String lastName, String avatar, String businessName, String taxId) {
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.email = email;
    this.userType = userType;
    this.firstName = firstName;
    this.lastName = lastName;
    this.avatar = avatar;
    this.businessName = businessName;
    this.taxId = taxId;
  }
}
