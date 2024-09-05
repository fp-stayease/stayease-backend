package com.finalproject.stayease.users.dto;

import com.finalproject.stayease.users.entity.Users;
import lombok.Data;

import java.time.Instant;

@Data
public class UsersResDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Users.UserType userType;
    private Instant createdAt;
}
