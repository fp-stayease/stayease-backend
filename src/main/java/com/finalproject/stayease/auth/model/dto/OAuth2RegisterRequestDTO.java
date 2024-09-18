package com.finalproject.stayease.auth.model.dto;

import com.finalproject.stayease.users.entity.Users.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuth2RegisterRequestDTO {
  private String googleToken;
  private UserType userType;
  private String businessName;
  private String taxId;
}
