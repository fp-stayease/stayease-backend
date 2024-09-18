package com.finalproject.stayease.transactions.dto.request;

import lombok.Data;

@Data
public class NotificationReqDTO {
    private String transaction_time;

    private String transaction_status;

    private String transaction_id;

    private String order_id;

    private String status_code;
}
