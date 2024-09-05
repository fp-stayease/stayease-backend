package com.finalproject.stayease.users.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TenantInfoResDto {
    private Long id;
    private UsersResDto user;
    private String businessName;
    private Instant registerDate;
}
