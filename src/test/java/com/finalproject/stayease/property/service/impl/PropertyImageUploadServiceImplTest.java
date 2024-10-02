package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PropertyImageUploadServiceImplTest {

  @Mock
  private CloudinaryService cloudinaryService;

  @InjectMocks
  private PropertyImageUploadServiceImpl imageUploadService;

  private MultipartFile validImage;
  private MultipartFile invalidTypeImage;
  private MultipartFile largeSizeImage;

  @BeforeEach
  void setUp() {
    validImage = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", new byte[1024]);
    invalidTypeImage = new MockMultipartFile("image.txt", "image.txt", "text/plain", new byte[1024]);
    largeSizeImage = new MockMultipartFile("large.jpg", "large.jpg", "image/jpeg", new byte[1024 * 1024 + 1]);
  }

  @Test
  void uploadImage_Success() throws IOException {
    when(cloudinaryService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("image_url");

    String result = imageUploadService.uploadImage(1L, validImage);

    assertEquals("image_url", result);
    verify(cloudinaryService).uploadFile(validImage, "tenants/id-1");
  }

  @Test
  void uploadImage_EmptyFile() {
    MultipartFile emptyFile = new MockMultipartFile("empty.jpg", new byte[0]);

    assertThrows(IllegalArgumentException.class, () -> imageUploadService.uploadImage(1L, emptyFile));
  }

  @Test
  void uploadImage_InvalidFileType() {
    assertThrows(IllegalArgumentException.class, () -> imageUploadService.uploadImage(1L, invalidTypeImage));
  }

  @Test
  void uploadImage_FileTooLarge() {
    assertThrows(IllegalArgumentException.class, () -> imageUploadService.uploadImage(1L, largeSizeImage));
  }

  @Test
  void uploadRoomImage_Success() throws IOException {
    when(cloudinaryService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("room_image_url");

    String result = imageUploadService.uploadRoomImage(1L, 2L, validImage);

    assertEquals("room_image_url", result);
    verify(cloudinaryService).uploadFile(validImage, "properties/id-1/rooms/id-2");
  }

  @Test
  void uploadRoomImage_InvalidFileType() {
    assertThrows(IllegalArgumentException.class, () -> imageUploadService.uploadRoomImage(1L, 2L, invalidTypeImage));
  }
}