package com.finalproject.stayease.reports.dto.overview;

import lombok.Data;

@Data
public class SummaryDTO {
    TrxDiffDTO trxDiff;
    UsersDiffDTO usersDiff;
    Long totalProperties;
    RevenueDiffDTO revenueDiff;

    public SummaryDTO(TrxDiffDTO trxDiff, UsersDiffDTO usersDiff, Long totalProperties, RevenueDiffDTO revenueDiff) {
        this.trxDiff = trxDiff;
        this.usersDiff = usersDiff;
        this.totalProperties = totalProperties;
        this.revenueDiff = revenueDiff;
    }
}
