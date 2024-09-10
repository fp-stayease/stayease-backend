package com.finalproject.stayease.users.entity.dto;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileDTO {
  private String id;
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String avatar;
  private Boolean isVerified;
  private Boolean isOAuth2;
  private UserType userType;
  private TenantInfoDTO tenantInfo;

  public UserProfileDTO(Users user) {
    this.id = user.getId().toString();
    this.email = user.getEmail();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.phoneNumber = user.getPhoneNumber();
    this.avatar = user.getAvatar();
    this.isVerified = user.getIsVerified();
    this.isOAuth2 = !user.getSocialLogins().isEmpty();
    this.userType = user.getUserType();
    if (user.getTenantInfo() != null) {
      this.tenantInfo = new TenantInfoDTO();
      this.tenantInfo.setBusinessName(user.getTenantInfo().getBusinessName());
      this.tenantInfo.setTaxId(user.getTenantInfo().getTaxId());
    }
  }


  @Data
  private static class TenantInfoDTO {
    private String businessName;
    private String taxId;
  }
}
