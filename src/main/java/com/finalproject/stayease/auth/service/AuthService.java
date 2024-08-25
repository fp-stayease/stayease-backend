package com.finalproject.stayease.auth.service;

import com.finalproject.stayease.auth.model.dto.LoginRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

  ResponseEntity<?> login(LoginRequestDTO loginRequestDTO);

  ResponseEntity<?> refreshToken(HttpServletRequest request);

  ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response);


}
