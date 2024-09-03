package com.finalproject.stayease.midtrans.dto;

import lombok.Data;

@Data
public class MidtransReqDto {
    private String payment_type;
    private TransactionDetail transaction_details;
    private BankTransfer bank_transfer;
}
