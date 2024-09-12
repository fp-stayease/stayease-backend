package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.users.service.UsersService;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Data
public class PropertyRoomImageCleanupService {

  private final CloudinaryService cloudinaryService;
  private final PropertyService propertyService;
  private final RoomService roomService;

  @SneakyThrows
  @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
  public void cleanupOrphanedImages() {
    Set<String> allUsersImages = getAllUsersImages();
    Set<String> allImagesInCloudinary = new HashSet<>(cloudinaryService.findAllImagesFromFolder("/users/*"));

    allImagesInCloudinary.removeAll(allUsersImages);

    for (String imageUrl : allImagesInCloudinary) {
      cloudinaryService.deleteImage(imageUrl);
    }
  }

  private Set<String> getAllUsersImages() {
    return new HashSet<>(usersService.findAllAvatars());
  }

}
