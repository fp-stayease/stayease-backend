package com.finalproject.stayease.users.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailChangeResponseDTO {

  private String verificationUrl;

}
