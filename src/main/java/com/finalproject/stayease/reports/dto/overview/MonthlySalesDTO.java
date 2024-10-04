package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class MonthlySalesDTO {
    private int month;
    private Double totalAmount;

    public MonthlySalesDTO(int month, Double totalAmount) {
        this.month = month;
        this.totalAmount = totalAmount;
    }
}
