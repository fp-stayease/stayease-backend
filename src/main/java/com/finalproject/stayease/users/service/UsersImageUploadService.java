package com.finalproject.stayease.users.service;

import com.finalproject.stayease.users.entity.Users;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface UsersImageUploadService {

  String uploadImage(MultipartFile images,  Users user) throws IOException;

}
