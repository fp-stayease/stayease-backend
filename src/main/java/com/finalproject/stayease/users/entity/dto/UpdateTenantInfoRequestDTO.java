package com.finalproject.stayease.users.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTenantInfoRequestDTO {

  private String businessName;
  private String taxId;
}
