package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.register.init.InitialRegistrationResponseDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.request.VerifyRegistrationDTO;
import com.finalproject.stayease.users.entity.dto.register.verify.response.VerifyUserResponseDTO;

public interface RegisterService {

  InitialRegistrationResponseDTO initialRegistration(InitialRegistrationRequestDTO requestDTO, UserType userType);
  VerifyUserResponseDTO verifyRegistration(VerifyRegistrationDTO verifyRegistrationDTO, String token);
}
