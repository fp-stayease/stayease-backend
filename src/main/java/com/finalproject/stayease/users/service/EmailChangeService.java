package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.dto.RequestEmailChangeDTO;
import jakarta.mail.MessagingException;
import java.io.IOException;

public interface EmailChangeService {
  String requestEmailChange(Users user, RequestEmailChangeDTO requestDTO) throws MessagingException, IOException;
  Users verifyEmailChange(String tokenUUID);
  boolean checkToken(String token);
}
