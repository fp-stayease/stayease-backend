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
  private String firstName;
  private String lastName;
  private TokenResponseDto token;

  public AuthResponseDto(Users user, TokenResponseDto token) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.userType = user.getUserType().toString();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.token = token;
  }
}
