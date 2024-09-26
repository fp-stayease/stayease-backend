package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class SummaryDTO {
    TrxDiffDTO trxDiff;
    UsersDiffDTO usersDiff;
    Long totalProperties;

    public SummaryDTO(TrxDiffDTO trxDiff, UsersDiffDTO usersDiff, Long totalProperties) {
        this.trxDiff = trxDiff;
        this.usersDiff = usersDiff;
        this.totalProperties = totalProperties;
    }
}
