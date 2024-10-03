package com.finalproject.stayease.auth.model.dto.register.verify.response;

import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class VerifyTenantResponseDTO extends VerifyUserResponseDTO {

  private Long tenantId;
  private String businessName;

  public VerifyTenantResponseDTO(Users user, TenantInfo tenantInfo) {
    super(user);
    this.tenantId = tenantInfo.getId();
    this.businessName = tenantInfo.getBusinessName();
  }

  public VerifyTenantResponseDTO toDTO(Users user, TenantInfo tenantInfo) {
    return new VerifyTenantResponseDTO(user, tenantInfo);
  }
}
