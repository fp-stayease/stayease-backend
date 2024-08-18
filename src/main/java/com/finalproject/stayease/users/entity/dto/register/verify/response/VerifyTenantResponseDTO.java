package com.finalproject.stayease.users.entity.dto.register.verify.response;

import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class VerifyTenantResponseDTO extends VerifyUserResponseDTO {

  private Long tenantId;
  private String businessName;

  public VerifyTenantResponseDTO(User user, TenantInfo tenantInfo) {
    super(user);
    this.tenantId = tenantInfo.getId();
    this.businessName = tenantInfo.getBusinessName();
  }

  public VerifyTenantResponseDTO toDTO(User user, TenantInfo tenantInfo) {
    return new VerifyTenantResponseDTO(user, tenantInfo);
  }
}
