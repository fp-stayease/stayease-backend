package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import jakarta.mail.MessagingException;
import java.io.IOException;

public interface ResetPasswordService {

  ForgotPasswordResponseDTO requestResetToken(ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException;

  ForgotPasswordResponseDTO requestResetTokenLoggedIn(ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException;

  void resetPassword(String randomKey, ResetPasswordRequestDTO requestDTO);

}
