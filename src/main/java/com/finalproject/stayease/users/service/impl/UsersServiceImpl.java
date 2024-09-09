package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.mail.service.MailService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import com.finalproject.stayease.users.repository.EmailChangeRedisRepository;
import com.finalproject.stayease.users.repository.UsersRepository;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Data
@Service
@Transactional
@Slf4j
public class UsersServiceImpl implements UsersService {

  private final UsersRepository usersRepository;
  private final EmailChangeRedisRepository emailChangeRedisRepository;
  private final MailService mailService;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  @Value("${FE_URL}")
  private String feUrl;

  public UsersServiceImpl(UsersRepository usersRepository, EmailChangeRedisRepository emailChangeRedisRepository, MailService mailService, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
    this.usersRepository = usersRepository;
    this.emailChangeRedisRepository = emailChangeRedisRepository;
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.mailService = mailService;
  }

  @Override
  public Optional<Users> findByEmail(String email) {
    return usersRepository.findByEmail(email);
  }

  @Override
  public Users getLoggedUser() throws RuntimeException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new AccessDeniedException("You must be logged in to access this resource");
    }
    String email = authentication.getName();
    return findByEmail(email).orElseThrow(() -> new AccessDeniedException("You must be logged in to access this resource"));
  }

  @Override
  public Users updateProfile(Users user, UpdateUserProfileRequestDTO requestDTO) {
    log.info("Updating user profile: " + requestDTO);
    Optional.ofNullable(requestDTO.getFirstName()).ifPresent(user::setFirstName);
    Optional.ofNullable(requestDTO.getLastName()).ifPresent(user::setLastName);
    Optional.ofNullable(requestDTO.getPhoneNumber()).ifPresent(user::setPhoneNumber);
    Optional.ofNullable(requestDTO.getAvatarUrl()).ifPresent(user::setAvatar);
    return usersRepository.save(user);
  }

  @Override
  public void changeAvatar(String imageUrl) {
    Users user = getLoggedUser();
    user.setAvatar(imageUrl);
    save(user);
  }

  @Override
  public void removeAvatar() {
    Users user = getLoggedUser();
    user.setAvatar(null);
    save(user);
  }

  @Override
  public String requestEmailChange(Users user, RequestEmailChangeDTO requestDTO) throws MessagingException, IOException {
    if (!user.getSocialLogins().isEmpty()) {
      // TODO : don't forget to change the one in password reset too!!
      throw new InvalidRequestException("Email change is not allowed for social login users");
    }
    if (findByEmail(requestDTO.getNewEmail()).isPresent()) {
      throw new InvalidRequestException("Email is already in use");
    }

    // * generate JWT token that contains userId and newEmail
    String emailChangeJwt = generateEmailChangeToken(user, requestDTO.getNewEmail());

    // * generate random token for URL param with UUID
    String tokenUUID = UUID.randomUUID().toString().replaceAll("-", "");

    // * save JWT in redis with tokenUUID as key
    emailChangeRedisRepository.saveToken(tokenUUID, emailChangeJwt);

    // * send email with URL containing tokenUUID
    sendVerificationEmail(requestDTO.getNewEmail(), tokenUUID);

    return buildVerificationUrl(tokenUUID);
  }

  @Override
  public void verifyEmailChange(String tokenUUID) {
    // * check if token is valid
    if (!emailChangeRedisRepository.isValid(tokenUUID)) {
      // TODO : make InvalidTokenException
      throw new RuntimeException("Invalid token");
    }

    // * get JWT from redis
    String emailChangeJwt = emailChangeRedisRepository.getJwt(tokenUUID);

    // * get prevEmail and newEmail from JWT
    String prevEmail = extractSubjectFromToken(emailChangeJwt);
    String newEmail = getNewEmailFromToken(emailChangeJwt);
    log.info("Email change request: {} -> {}", prevEmail, newEmail);

    // * update user email
    // TODO : make findByEmail return UserNotFoundException
    Users user = findByEmail(prevEmail).orElseThrow(() -> new RuntimeException("User not found"));
    user.setEmail(newEmail);
    save(user);

    // * mark token as verified
    emailChangeRedisRepository.verifyEmail(tokenUUID);
  }

  @Override
  public List<String> findAllAvatars() {
    return usersRepository.findAllAvatars();
  }

  @Override
  public Optional<Users> findById(Long id) {
    return usersRepository.findById(id);
  }

  @Override
  public void save(Users user) {
    usersRepository.save(user);
  }

  private void sendVerificationEmail(String newEmail, String tokenUUID) throws MessagingException, IOException {
    // * send email with URL containing tokenUUID
    // Load the HTML template
    Resource resource = new ClassPathResource("templates/email-change.html");
    String htmlTemplate = Files.readString(resource.getFile().toPath());

    // Replace placeholders with actual values
    String htmlContent = htmlTemplate
        .replace("${verificationUrl}", buildVerificationUrl(tokenUUID));

    String subject = "Verify your new email!";

    mailService.sendHtmlEmail(htmlContent, newEmail, subject);
  }

  private String buildVerificationUrl(String tokenUUID) {
    // TODO : configure with feUrl if necessary
    return feUrl + "/verify-email?token=" + tokenUUID;
  }

  public String generateEmailChangeToken(Users user, String newEmail) {

    JwtClaimsSet claimsSet = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
        .subject(user.getEmail())
        .claim("newEmail", newEmail)
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }

  public String extractSubjectFromToken(String token) {
    return jwtDecoder.decode(token).getSubject();
  }

  public String getNewEmailFromToken(String token) {
    return jwtDecoder.decode(token).getClaim("newEmail");
  }

}
