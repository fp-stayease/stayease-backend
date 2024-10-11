package com.finalproject.stayease.auth.service.helpers;

import com.finalproject.stayease.exceptions.auth.InvalidCredentialsException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.users.entity.SocialLogin;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.SocialLoginService;
import com.finalproject.stayease.users.service.UsersService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

  private final UsersService usersService;
  private final SocialLoginService socialLoginService;

  /**
   * Checks if the user exists and is not a social login user.
   */
  public void checkUser(String email) {
    Optional<Users> usersOptional = usersService.findByEmail(email);
    if (usersOptional.isEmpty()) {
      throw new UsernameNotFoundException("User not found");
    }
    Optional<SocialLogin> socialLoginOptional = socialLoginService.findByUser(usersOptional.get());
    if (socialLoginOptional.isPresent()) {
      throw new InvalidRequestException("Not allowed to change password for social login users");
    }
  }

  /**
   * Checks if the logged-in user matches the provided email.
   */
  public void checkLoggedInUser(String email) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      String emailFromAuthentication = authentication.getName();
      if (!email.equals(emailFromAuthentication)) {
        throw new InvalidCredentialsException("Email does not match");
      }
      checkUser(email);
    }
  }
}
