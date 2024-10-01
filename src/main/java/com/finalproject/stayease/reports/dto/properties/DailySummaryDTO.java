package com.finalproject.stayease.reports.dto.properties;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Data
public class DailySummaryDTO {
    private String date;
    private Double totalPrice;

    public DailySummaryDTO(String date, Double totalPrice) {
        this.date = date;
        this.totalPrice = totalPrice;
    }
}
