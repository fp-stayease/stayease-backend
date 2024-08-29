package com.finalproject.stayease.transactions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class NotificationReqDto {
    private String transaction_time;

    private String transaction_status;

    private String transaction_id;

    private String order_id;

    private String status_code;
}
