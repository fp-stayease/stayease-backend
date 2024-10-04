package com.finalproject.stayease.reports.dto.properties;

import lombok.Data;

@Data
public class DailySummaryDTO {
    private String date;
    private Double totalPrice;

    public DailySummaryDTO(String date, Double totalPrice) {
        this.date = date;
        this.totalPrice = totalPrice;
    }
}
