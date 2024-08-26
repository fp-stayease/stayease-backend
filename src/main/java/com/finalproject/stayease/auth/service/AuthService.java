package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import com.finalproject.stayease.auth.model.dto.LoginResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

  ResponseEntity<?> login(LoginRequestDTO loginRequestDTO);

  LoginResponseDTO refreshToken(HttpServletRequest request, HttpServletResponse response);

  ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response);


}
