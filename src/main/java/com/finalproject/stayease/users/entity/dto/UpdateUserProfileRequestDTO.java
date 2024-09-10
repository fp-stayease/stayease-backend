package com.finalproject.stayease.users.entity.dto;

import lombok.Data;

@Data
public class UpdateUserProfileRequestDTO {
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String avatarUrl;

}
