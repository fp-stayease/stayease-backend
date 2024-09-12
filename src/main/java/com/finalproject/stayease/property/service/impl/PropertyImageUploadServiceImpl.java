package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.service.PropertyImageUploadService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@Slf4j
@Data
public class PropertyImageUploadServiceImpl implements PropertyImageUploadService {

  private final CloudinaryService cloudinaryService;
  private final PropertyService propertyService;
  private final RoomService roomService;

  // Allowed file types
  private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/jpg",
      "image/png", "image/gif");
  // Max file size (1MB)
  private static final long MAX_FILE_SIZE = 1024 * 1024;


  @Override
  public String uploadImage(Long propertyId, MultipartFile image) throws IOException {
    log.info("Validating file: {}", image.getOriginalFilename());
    validateFile(image);
    String folderName = "properties/id-" + propertyId;
    log.info("Uploading image: {} to folder: {}", image.getOriginalFilename(), folderName);
    return cloudinaryService.uploadFile(image, folderName);
  }

  @Override
  public String uploadRoomImage(Long propertyId, Long roomId, MultipartFile image) throws IOException {
    log.info("Validating file for room image: {}", image.getOriginalFilename());
    validateFile(image);
    String folderName = "properties/id-" + propertyId  + "/rooms/id-" + roomId;
    log.info("Uploading image for room: {} to folder: {}", image.getOriginalFilename(), folderName);
    return cloudinaryService.uploadFile(image, folderName);
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
      throw new IllegalArgumentException("Invalid file type");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("File size exceeds maximum limit");
    }
  }
}
