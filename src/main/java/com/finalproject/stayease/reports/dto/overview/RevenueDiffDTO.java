package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class RevenueDiffDTO {
    private Double revenueThisMonth;
    private Double revenueDiffPercent;

    public RevenueDiffDTO(Double revenueThisMonth, Double revenueDiffPercent) {
        this.revenueThisMonth = revenueThisMonth;
        this.revenueDiffPercent = revenueDiffPercent;
    }
}
