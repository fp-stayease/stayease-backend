package com.finalproject.stayease.midtrans.dto;

import lombok.Data;

@Data
public class MidtransReqDTO {
    private String payment_type;
    private TransactionDetailDTO transaction_details;
    private BankTransferDTO bank_transfer;
}
