package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class TrxDiffDTO {
    private Double trxThisMonth;
    private Double trxDiffPercent;

    public TrxDiffDTO(Double trxThisMonth, Double trxDiffPercent) {
        this.trxThisMonth = trxThisMonth;
        this.trxDiffPercent = trxDiffPercent;
    }
}
