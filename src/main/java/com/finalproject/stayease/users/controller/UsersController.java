package com.finalproject.stayease.users.controller;

import static com.finalproject.stayease.auth.util.SessionCookieUtil.invalidateSessionAndCookie;

import com.finalproject.stayease.auth.model.dto.request.TokenRequestDTO;
import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.EmailChangeResponseDTO;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import com.finalproject.stayease.users.entity.dto.UpdateTenantInfoRequestDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import com.finalproject.stayease.users.entity.dto.UsersImageDTO;
import com.finalproject.stayease.users.entity.dto.UsersProfileDTO;
import com.finalproject.stayease.users.service.EmailChangeService;
import com.finalproject.stayease.users.service.ProfileService;
import com.finalproject.stayease.users.service.UsersImageUploadService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.annotation.Nullable;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Data
@Slf4j
public class UsersController {

  private final UsersService usersService;
  private final UsersImageUploadService usersImageUploadService;
  private final EmailChangeService emailChangeService;
  private final ProfileService profileService;

  @DeleteMapping
  public ResponseEntity<Response<Object>> deleteUser(HttpServletRequest request, HttpServletResponse response) {
    Users loggedUser = usersService.getLoggedUser();
    usersService.deleteUser(loggedUser);
    invalidateSessionAndCookie(request, response);
    return Response.successfulResponse(HttpStatus.OK.value(), "User deleted successfully!", null);
  }

  @GetMapping("/profile")
  public ResponseEntity<Response<UsersProfileDTO>> getLoggedInProfile() {
    Users loggedUser = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.OK.value(), "Profile retrieved successfully!", new UsersProfileDTO(loggedUser));
  }

  @PutMapping("/profile")
  public ResponseEntity<Response<UsersProfileDTO>> updateProfile(@RequestBody UpdateUserProfileRequestDTO requestDTO) {
    Users loggedUser = usersService.getLoggedUser();
    Users updatedUser = profileService.updateProfile(loggedUser, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Profile updated successfully!", new UsersProfileDTO(updatedUser));
  }

  @PutMapping("/profile/tenant")
  public ResponseEntity<Response<UsersProfileDTO>> updateTenantInfo(@RequestBody UpdateTenantInfoRequestDTO requestDTO) {
    Users loggedUser = usersService.getLoggedUser();
    Users updatedUser = profileService.updateTenantInfo(loggedUser, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Tenant info updated successfully!", new UsersProfileDTO(updatedUser));
  }

  @PostMapping("/profile/avatar")
  public ResponseEntity<Response<UsersImageDTO>> uploadAvatar (@RequestBody MultipartFile image)
      throws IOException {
    Users loggedUser = usersService.getLoggedUser();
    String imageUrl = usersImageUploadService.uploadImage(image, loggedUser);
    log.info("Avatar uploaded successfully! With imageUrl: {}", imageUrl);
    return Response.successfulResponse(HttpStatus.OK.value(), "Avatar uploaded successfully!",
        new UsersImageDTO(imageUrl));
  }

  @PutMapping("/profile/avatar")
  public ResponseEntity<Response<UsersProfileDTO>> updateAvatar(@Nullable @RequestBody UsersImageDTO requestDTO) {
    Users loggedInUser = usersService.getLoggedUser();
    log.info("Updating avatar for user: {}, with avatarUrl: {}", loggedInUser.getEmail(), requestDTO);
    Users updatedUser;
    if (requestDTO == null) {
      updatedUser = profileService.removeAvatar(loggedInUser);
      log.info("Avatar removed successfully!");
      return Response.successfulResponse(HttpStatus.OK.value(), "Avatar removed successfully!", new UsersProfileDTO(updatedUser));
    } else {
      updatedUser = profileService.changeAvatar(loggedInUser, requestDTO.getAvatarUrl());
      log.info("Avatar updated successfully!");
    }
    return Response.successfulResponse(HttpStatus.OK.value(), "Avatar updated successfully!", new UsersProfileDTO(updatedUser));
  }

  @PostMapping("/profile/email")
  public ResponseEntity<Response<EmailChangeResponseDTO>> requestEmailChange(@RequestBody RequestEmailChangeDTO requestDTO)
      throws MessagingException, IOException {
    Users loggedUser = usersService.getLoggedUser();
    String verificationUrl = emailChangeService.requestEmailChange(loggedUser, requestDTO);
    log.info("Email change request sent successfully! Verification URL: {}", verificationUrl);
    return Response.successfulResponse(HttpStatus.OK.value(), "Email change request sent successfully!", new EmailChangeResponseDTO(verificationUrl));
  }

  @PutMapping("/profile/email")
  public ResponseEntity<Response<UsersProfileDTO>> verifyEmailChange(@RequestParam("token") String tokenUUID,
      HttpServletRequest request, HttpServletResponse response) {
    Users user = emailChangeService.verifyEmailChange(tokenUUID);
    invalidateSessionAndCookie(request, response);
    return Response.successfulResponse(HttpStatus.OK.value(), "Email changed successfully! Please log back in with "
                                                              + "your new credentials!", new UsersProfileDTO(user));
  }

  @PostMapping("/profile/email/check-token")
  public ResponseEntity<Response<Boolean>> checkEmailChangeToken(@RequestBody TokenRequestDTO tokenRequestDTO) {
    String token = tokenRequestDTO.getToken();
    String normalizedToken = token.replaceAll("=+$", "");
    boolean isValid = emailChangeService.checkToken(normalizedToken);
    String message = isValid ? "Token is valid" : "Token is invalid, please check your email or try to resend an "
                                                  + "email change request.";
    log.info("Token: {}, isValid: {}", normalizedToken, isValid);
    return Response.successfulResponse(HttpStatus.OK.value(), message, isValid);
  }



}
