package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class TrxDiffDTO {
    private Long trxThisMonth;
    private Long trxDiffPercent;

    public TrxDiffDTO(Long trxThisMonth, Long trxDiffPercent) {
        this.trxThisMonth = trxThisMonth;
        this.trxDiffPercent = trxDiffPercent;
    }
}
