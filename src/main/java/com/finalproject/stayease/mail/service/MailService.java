package com.finalproject.stayease.mail.service;

import com.finalproject.stayease.mail.model.MailTemplate;
import com.finalproject.stayease.pdf.PdfService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;
  private final PdfService pdfService;

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

  public void sendMailWithPdf(String to, String subject, String templateName, Map<String, String> templateData) throws MessagingException, IOException {
    String htmlTemplate = pdfService.loadHtmlTemplate(templateName);

    String filledHtmlTemplate = pdfService.fillTemplate(htmlTemplate, templateData);
    byte[] pdfBytes = pdfService.generatePdfFromHtml(filledHtmlTemplate);

    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
    helper.setFrom(MAIL_USERNAME);
    helper.setTo(to);
    helper.setSubject(subject);

    helper.addAttachment("Stay_Ease_Booking_Invoice.pdf", new ByteArrayResource(pdfBytes));

    mailSender.send(mimeMessage);
  }
}
