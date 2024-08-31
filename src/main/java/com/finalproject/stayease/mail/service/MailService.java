package com.finalproject.stayease.mail.service;

import com.finalproject.stayease.mail.model.MailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
  @Value("${BASE_URL}")
  private String baseUrl;
  @Value("${API_VERSION}")
  private String apiVersion;

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

  public void sendEmailVerification(String toEmail, String verificationUrl)
      throws MessagingException, IOException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

    // Load the HTML template
    Resource resource = new ClassPathResource("templates/verification-email.html");
    String htmlTemplate = Files.readString(resource.getFile().toPath());

    // Replace placeholders with actual values
    String htmlContent = htmlTemplate
        .replace("${verificationUrl}", verificationUrl)
        // TODO : Replace with FE URL
        .replace("${feURL}", baseUrl + "/" + apiVersion);

    helper.setFrom(MAIL_USERNAME);
    helper.setText(htmlContent, true);
    helper.setTo(toEmail);
    helper.setSubject("Email Verification");

    mailSender.send(mimeMessage);
  }
}
