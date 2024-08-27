package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.LoginResponseDTO;

public interface AuthService {

  LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

  void logout(String email);
}
