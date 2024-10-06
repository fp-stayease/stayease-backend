package com.finalproject.stayease.reports.dto.properties;

import lombok.Data;

@Data
public class PropertiesSalesDTO {
    private Double revenue;
    private Double tax;

    public PropertiesSalesDTO(Double revenue, Double tax) {
        this.revenue = revenue;
        this.tax = tax;
    }
}
