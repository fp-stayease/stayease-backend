package com.finalproject.stayease.mail.service;

import com.finalproject.stayease.mail.model.MailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String MAIL_USERNAME;

  public void sendMail(MailTemplate mailTemplate) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(MAIL_USERNAME);
    message.setTo(mailTemplate.getTo());
    message.setSubject(mailTemplate.getSubject());
    message.setText(mailTemplate.getMessage());
    mailSender.send(message);
  }

  public void sendMail(SimpleMailMessage message) {
    message.setFrom(MAIL_USERNAME);
    mailSender.send(message);
  }

  public void sendMimeMessage(String to, String subject, String htmlMsg)
      throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
    helper.setFrom(MAIL_USERNAME);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlMsg, true);
    mailSender.send(mimeMessage);
  }
}
