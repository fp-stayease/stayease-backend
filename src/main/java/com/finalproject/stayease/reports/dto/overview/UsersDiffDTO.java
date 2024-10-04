package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class UsersDiffDTO {
    private Long usersThisMonth;
    private Long usersDiffPercent;

    public UsersDiffDTO(Long usersThisMonth, Long usersDiffPercent) {
        this.usersThisMonth = usersThisMonth;
        this.usersDiffPercent = usersDiffPercent;
    }
}
