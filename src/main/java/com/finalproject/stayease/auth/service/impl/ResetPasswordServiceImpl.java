package com.finalproject.stayease.auth.service.impl;

import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordRequestDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.request.ForgotPasswordResponseDTO;
import com.finalproject.stayease.auth.model.dto.forgorPassword.reset.ResetPasswordRequestDTO;
import com.finalproject.stayease.auth.service.ResetPasswordService;
import com.finalproject.stayease.auth.service.helpers.ResetPasswordHelper;
import com.finalproject.stayease.auth.service.helpers.TokenGenerator;
import com.finalproject.stayease.auth.service.helpers.UserValidator;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
@Transactional
public class ResetPasswordServiceImpl implements ResetPasswordService {

  private final ResetPasswordHelper resetPasswordHelper;
  private final UserValidator userValidator;
  private final TokenGenerator tokenGenerator;

  @Value("${FE_URL}")
  private String feUrl;
  @Value("${token.expire.hours:1}")
  private long TOKEN_EXPIRE;

  /**
   * Requests a password reset token for a given email.
   */
  @Override
  public ForgotPasswordResponseDTO requestResetToken(ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException {
    String email = requestDTO.getEmail();
    userValidator.checkUser(email);
    return resetPasswordHelper.handleResetTokenRequest(email);
  }

  /**
   * Requests a password reset token for a logged-in user.
   */
  @Override
  public ForgotPasswordResponseDTO requestResetTokenLoggedIn(ForgotPasswordRequestDTO requestDTO)
      throws MessagingException, IOException {
    String email = requestDTO.getEmail();
    userValidator.checkLoggedInUser(email);
    return resetPasswordHelper.handleResetTokenRequest(email);
  }

  /**
   * Resets the password using the provided token and new password.
   */
  @Override
  public void resetPassword(String randomKey, ResetPasswordRequestDTO requestDTO) {
    resetPasswordHelper.performPasswordReset(randomKey, requestDTO);
  }

  /**
   * Checks if a given token is valid.
   */
  @Override
  public boolean checkToken(String token) {
    return tokenGenerator.isTokenValid(token);
  }
}
