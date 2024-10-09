package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class UsersDiffDTO {
    private Double usersThisMonth;
    private Double usersDiffPercent;

    public UsersDiffDTO(Double usersThisMonth, Double usersDiffPercent) {
        this.usersThisMonth = usersThisMonth;
        this.usersDiffPercent = usersDiffPercent;
    }
}
