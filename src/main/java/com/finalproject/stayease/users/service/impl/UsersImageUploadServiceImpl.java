package com.finalproject.stayease.users.service.impl;

import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersImageUploadService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Data
@Transactional
@Slf4j
public class UsersImageUploadServiceImpl implements UsersImageUploadService {

  private final CloudinaryService cloudinaryService;

  // Allowed file types
  private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/jpg",
      "image/png", "image/gif");
  // Max file size (1MB)
  private static final long MAX_FILE_SIZE = 1024 * 1024;

  @Override
  public String uploadImage(MultipartFile image, Users user) throws IOException {
    log.info("Validating file: {}", image.getOriginalFilename());
    validateFile(image);
    String folderName = "users/id-" + user.getId();
    log.info("Uploading image: {} to folder: {}", image.getOriginalFilename(), folderName);
    return cloudinaryService.uploadFile(image, folderName);
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new InvalidRequestException("File is empty");
    }
    if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
      throw new InvalidRequestException("Invalid file type");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new InvalidRequestException("File size exceeds maximum limit");
    }
  }

}
