package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.auth.model.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.auth.model.dto.register.verify.response.VerifyUserResponseDTO;
import jakarta.mail.MessagingException;
import java.io.IOException;

public interface RegisterService {

  InitialRegistrationResponseDTO initialRegistration(InitialRegistrationRequestDTO requestDTO, UserType userType)
      throws MessagingException, IOException;
  VerifyUserResponseDTO verifyRegistration(VerifyRegistrationDTO verifyRegistrationDTO, String token);
}
