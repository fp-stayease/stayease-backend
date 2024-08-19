package com.finalproject.stayease.auth.dto;

import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginResponse {
  private Long userId;
  private String email;
  private String firstName;
  private String lastName;
  private User.UserType userType;
  private boolean isNewUser;
  private TenantInfo tenantInfo;

  // TODO: this :(
  private String token;  // JWT token for authentication

  public SocialLoginResponse(User user, String token) {
    this.userId = user.getId();
    this.email = user.getEmail();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.userType = user.getUserType();
    this.isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());
    this.token = token;
  }



  public SocialLoginResponse(User user, TenantInfo tenantInfo, String token) {
    this.userId = user.getId();
    this.email = user.getEmail();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.userType = user.getUserType();
    this.isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());
    this.tenantInfo = tenantInfo;

    // TODO!!
    this.token = token;
  }
}
