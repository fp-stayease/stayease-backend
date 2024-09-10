package com.finalproject.stayease.users.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.EmailChangeResponseDTO;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import com.finalproject.stayease.users.entity.dto.UserProfileDTO;
import com.finalproject.stayease.users.entity.dto.UsersImageDTO;
import com.finalproject.stayease.users.service.EmailChangeService;
import com.finalproject.stayease.users.service.ProfileService;
import com.finalproject.stayease.users.service.UsersImageUploadService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UsersController {

  private final UsersService usersService;
  private final UsersImageUploadService usersImageUploadService;
  private final EmailChangeService emailChangeService;
  private final ProfileService profileService;

  @GetMapping("/profile")
  public ResponseEntity<Response<UserProfileDTO>> getLoggedInProfile() {
    Users loggedUser = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.OK.value(), "Profile retrieved successfully!", new UserProfileDTO(loggedUser));
  }

  @PutMapping("/profile")
  public ResponseEntity<Response<UserProfileDTO>> updateProfile(@RequestBody UpdateUserProfileRequestDTO requestDTO) {
    Users loggedUser = usersService.getLoggedUser();
    Users updatedUser = profileService.updateProfile(loggedUser, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Profile updated successfully!", new UserProfileDTO(updatedUser));
  }

  @PostMapping("/profile/avatar")
  public ResponseEntity<Response<UsersImageDTO>> uploadAvatar (@RequestBody MultipartFile image)
      throws IOException {
    Users loggedUser = usersService.getLoggedUser();
    String imageUrl = usersImageUploadService.uploadImage(image, loggedUser);
    return Response.successfulResponse(HttpStatus.OK.value(), "Avatar uploaded successfully!",
        new UsersImageDTO(imageUrl));
  }

  @PutMapping("/profile/avatar")
  public ResponseEntity<Response<UserProfileDTO>> updateAvatar(@RequestBody UsersImageDTO requestDTO) {
    Users loggedInUser = usersService.getLoggedUser();
    Users updatedUser;
    if (requestDTO.getAvatarUrl() == null) {
      updatedUser = profileService.removeAvatar(loggedInUser);
      return Response.successfulResponse(HttpStatus.OK.value(), "Avatar removed successfully!", new UserProfileDTO(updatedUser));
    } else {
      updatedUser = profileService.removeAvatar(loggedInUser);
      profileService.changeAvatar(loggedInUser, requestDTO.getAvatarUrl());
    }
    return Response.successfulResponse(HttpStatus.OK.value(), "Avatar updated successfully!", new UserProfileDTO(updatedUser));
  }

  @PostMapping("/profile/email")
  public ResponseEntity<Response<EmailChangeResponseDTO>> requestEmailChange(@RequestBody RequestEmailChangeDTO requestDTO)
      throws MessagingException, IOException {
    Users loggedUser = usersService.getLoggedUser();
    String verificationUrl = emailChangeService.requestEmailChange(loggedUser, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Email change request sent successfully!", new EmailChangeResponseDTO(verificationUrl));
  }

  @PutMapping("/profile/email")
  public ResponseEntity<Response<UserProfileDTO>> verifyEmailChange(@RequestParam("token") String tokenUUID,
      HttpServletRequest request, HttpServletResponse response) {
    Users user = emailChangeService.verifyEmailChange(tokenUUID);
    invalidateSessionAndCookie(request, response);
    return Response.successfulResponse(HttpStatus.OK.value(), "Email changed successfully! Please log back in with "
                                                              + "your new credentials!", new UserProfileDTO(user));
  }

  private void invalidateSessionAndCookie(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
    }
  }


}
