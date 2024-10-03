package com.finalproject.stayease.auth.model.dto;

import com.finalproject.stayease.users.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {

  private Long id;
  private String email;
  private String userType;
  private Boolean isVerified;
  private String firstName;
  private String lastName;
  private String avatarUrl;
  private Boolean isOAuth2;
  private TokenResponseDto token;

  public AuthResponseDto(Users user, TokenResponseDto token) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.userType = user.getUserType().toString();
    this.isVerified = user.getIsVerified();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.avatarUrl = user.getAvatar();
    this.isOAuth2 = !user.getSocialLogins().isEmpty();
    this.token = token;
  }
}
