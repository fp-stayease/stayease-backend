package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.User.UserType;
import com.finalproject.stayease.users.entity.dto.InitialRegistrationRequestDTO;
import com.finalproject.stayease.users.entity.dto.InitialRegistrationResponseDTO;

public interface RegisterService {

  InitialRegistrationResponseDTO initialRegistration(InitialRegistrationRequestDTO requestDTO, UserType userType);
}
