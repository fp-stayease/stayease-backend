package com.finalproject.stayease.transactions.dto;

import lombok.Data;

@Data
public class NotificationReqDto {
    private String transaction_time;

    private String transaction_status;

    private String transaction_id;

    private String order_id;

    private String status_code;
}
