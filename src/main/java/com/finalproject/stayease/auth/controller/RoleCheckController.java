package com.finalproject.stayease.auth.controller;

import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.entity.Users.UserType;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/role")
@Data
public class RoleCheckController {

  private final UsersService usersService;

  @GetMapping("/user")
  public String user(HttpServletResponse response) {
    Users user = usersService.getLoggedUser();
    if (user.getUserType().equals(UserType.USER)) {
      return "Welcome USER: " + user.getEmail();
    } else if (user.getUserType().equals(UserType.TENANT)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return "You are attempting to log in as a user, please use the correct endpoint to login as a TENANT";
    }
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return "Please log in to continue";
  }

  @GetMapping("/tenant")
  public String tenant(HttpServletResponse response) {
    Users user = usersService.getLoggedUser();
    if (user.getUserType().equals(UserType.TENANT)) {
      return "Welcome TENANT: " + user.getEmail();
    } else if (user.getUserType().equals(UserType.USER)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return "You are attempting to log in as a user, please use the correct endpoint to login as a USER";
    }
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return "Please log in to continue";
  }

  @GetMapping
  public String role(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return "You are not allowed to access this resource";
  }
}
