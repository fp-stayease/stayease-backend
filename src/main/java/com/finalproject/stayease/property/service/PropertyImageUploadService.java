package com.finalproject.stayease.property.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface PropertyImageUploadService {

  String uploadImage(Long tenantId, MultipartFile image) throws IOException;
  String uploadRoomImage(Long propertyId, Long roomId, MultipartFile image) throws IOException;
}
