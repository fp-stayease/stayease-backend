package com.finalproject.stayease.auth.model.dto;

import com.finalproject.stayease.users.entity.Users.UserType;
import lombok.Data;

@Data
public class SocialSelectUserTypeDTO {

  private UserType userType;

}
