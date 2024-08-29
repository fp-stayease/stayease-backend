package com.finalproject.stayease.mail.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MailTemplate {

  private String to;
  private String subject;
  private String message;

}
