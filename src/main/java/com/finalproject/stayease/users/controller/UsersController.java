package com.finalproject.stayease.users.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.EmailChangeResponseDTO;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import com.finalproject.stayease.users.entity.dto.UpdateUserProfileRequestDTO;
import com.finalproject.stayease.users.entity.dto.UserProfileDTO;
import com.finalproject.stayease.users.entity.dto.UsersImageDTO;
import com.finalproject.stayease.users.service.UsersImageUploadService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.mail.MessagingException;
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

  @GetMapping("/profile")
  public ResponseEntity<Response<UserProfileDTO>> getLoggedInProfile() {
    Users loggedUser = usersService.getLoggedUser();
    return Response.successfulResponse(HttpStatus.OK.value(), "Profile retrieved successfully!", new UserProfileDTO(loggedUser));
  }

  @PutMapping("/profile")
  public ResponseEntity<Response<UserProfileDTO>> updateProfile(@RequestBody UpdateUserProfileRequestDTO requestDTO) {
    Users loggedUser = usersService.getLoggedUser();
    Users updatedUser = usersService.updateProfile(loggedUser, requestDTO);
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
  public ResponseEntity<Response<Object>> updateAvatar(@RequestBody UsersImageDTO requestDTO) {
    if (requestDTO.getAvatarUrl() == null) {
      usersService.removeAvatar();
      return Response.successfulResponse(HttpStatus.OK.value(), "Avatar removed successfully!", null);
    } else {
      usersService.changeAvatar(requestDTO.getAvatarUrl());
    }
    return Response.successfulResponse(HttpStatus.OK.value(), "Avatar updated successfully!", null);
  }

  @PostMapping("/profile/email")
  public ResponseEntity<Response<EmailChangeResponseDTO>> requestEmailChange(@RequestBody RequestEmailChangeDTO requestDTO)
      throws MessagingException, IOException {
    Users loggedUser = usersService.getLoggedUser();
    String verificationUrl = usersService.requestEmailChange(loggedUser, requestDTO);
    return Response.successfulResponse(HttpStatus.OK.value(), "Email change request sent successfully!", new EmailChangeResponseDTO(verificationUrl));
  }

  @PutMapping("/profile/email")
  public ResponseEntity<Response<Object>> verifyEmailChange(@RequestParam("token") String tokenUUID) {
    usersService.verifyEmailChange(tokenUUID);
    return Response.successfulResponse(HttpStatus.OK.value(), "Email changed successfully!", null);
  }


}
