package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.dto.InitialRegistrationResponseDTO;

public interface UserService {

  InitialRegistrationResponseDTO initialRegistration(String email, String role);
}
