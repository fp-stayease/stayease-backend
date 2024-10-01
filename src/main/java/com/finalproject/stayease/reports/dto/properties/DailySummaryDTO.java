package com.finalproject.stayease.reports.dto.properties;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Data
public class DailySummaryDTO {
    private String date;
    private int month;
    private Double totalPrice;

    public DailySummaryDTO(String date, int month, Double totalPrice) {
        this.date = date;
        this.month = month;
        this.totalPrice = totalPrice;
    }

    public DailySummaryDTO(int month, Double totalPrice) {
        this.date = "";
        this.month = month;
        this.totalPrice = totalPrice;
    }
}
