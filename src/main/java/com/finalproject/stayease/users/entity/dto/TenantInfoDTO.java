package com.finalproject.stayease.users.entity.dto;

import com.finalproject.stayease.users.entity.TenantInfo;
import lombok.Data;

import java.time.Instant;

@Data
public class TenantInfoDTO {
    private String businessName;
    private String taxId;
    private Instant registrationDate;

    public TenantInfoDTO(TenantInfo tenantInfo) {
        this.businessName = tenantInfo.getBusinessName();
        this.taxId = tenantInfo.getTaxId();
        this.registrationDate = tenantInfo.getRegistrationDate();
    }
}
