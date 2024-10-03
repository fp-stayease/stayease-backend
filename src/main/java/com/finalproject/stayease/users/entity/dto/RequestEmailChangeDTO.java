package com.finalproject.stayease.users.entity.dto;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestEmailChangeDTO {

  @NotEmpty(message = "New email must not be empty" )
  private String newEmail;

}
