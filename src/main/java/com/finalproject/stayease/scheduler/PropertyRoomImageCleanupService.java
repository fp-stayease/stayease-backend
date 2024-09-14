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

  @SneakyThrows
  @Scheduled(cron = "${cron.cleanup.cloudinary:0 0 * * * ?}") // Runs every hour
  public void cleanupOrphanedImages() {
    log.info("Starting orphaned image cleanup");
    Set<String> allPropertyAndRoomImages = getAllPropertyAndRoomImages();
    log.info("image examples: {}", allPropertyAndRoomImages.stream().findFirst().orElse("No images found"));
    Set<String> allImagesInCloudinary = new HashSet<>(cloudinaryService.findAllImagesFromFolder("tenants/"));
    log.info("cloudinary examples: {}", allImagesInCloudinary.stream().findFirst().orElse("No images found"));

    allImagesInCloudinary.removeAll(allPropertyAndRoomImages);

    for (String imageUrl : allImagesInCloudinary) {
      cloudinaryService.deleteImage(imageUrl);
      log.info("Deleted orphaned image: {}", imageUrl);
    }
  }

  private Set<String> getAllPropertyAndRoomImages() {
    return new HashSet<>(propertyService.findAllPropertyRoomImageUrls());
  }

}
