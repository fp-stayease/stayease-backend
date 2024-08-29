package com.finalproject.stayease.users.entity.dto.register.verify.response;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import java.time.Instant;
import lombok.Data;

@Data
public class VerifyUserResponseDTO {

  private Long userId;
  private String email;
  private String firstName;
  private String lastName;
  private UserType userType;
  private Instant registeredAt;

  public VerifyUserResponseDTO(Users user) {
    this.userId = user.getId();
    this.email = user.getEmail();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.userType = user.getUserType();
    this.registeredAt = user.getCreatedAt();
  }

  public VerifyUserResponseDTO toDTO(Users user) {
    return new VerifyUserResponseDTO(user);
  }

}
