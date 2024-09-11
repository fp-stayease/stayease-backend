package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.TokenResponseDto;

public interface AuthService {

  TokenResponseDto login(LoginRequestDTO loginRequestDTO);

  void logout(String email);

  TokenResponseDto generateTokenFromEmail(String email);
}
