package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;

public interface ResetPasswordService {

  ForgotPasswordResponseDTO requestResetToken(ForgotPasswordRequestDTO requestDTO);

  void resetPassword(String randomKey, ResetPasswordRequestDTO requestDTO);
}
