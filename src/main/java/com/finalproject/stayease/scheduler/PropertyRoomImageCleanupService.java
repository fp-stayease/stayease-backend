package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.property.service.PropertyService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
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
//  @Scheduled(cron = "${cron.cleanup.cloudinary:0 0 * * * ?}") // Runs every hour
//  @Scheduled(cron = "0 */1 * * * ?") // Runs every 5 minutes
  public void cleanupOrphanedImages() {
    log.info("Starting orphaned image cleanup");
    Set<String> allPropertyAndRoomImagesPublicIds = extractPublicIdsFromUrls(getAllPropertyAndRoomImages());
    log.info("All property and room image public IDs: {}", allPropertyAndRoomImagesPublicIds);

    Set<String> allImagesInCloudinaryPublicIds = extractPublicIdsFromUrls(new HashSet<>(cloudinaryService.findAllImagesFromFolder("tenants/")));
    log.info("All images in Cloudinary public IDs: {}", allImagesInCloudinaryPublicIds);

    allImagesInCloudinaryPublicIds.removeAll(allPropertyAndRoomImagesPublicIds);
    log.info("Orphaned images in Cloudinary public IDs: {}", allImagesInCloudinaryPublicIds);

    for (String imagePublicId : allImagesInCloudinaryPublicIds) {
      cloudinaryService.deleteImage(imagePublicId);
      log.info("Deleted orphaned image: {}", imagePublicId);
    }
  }

  private Set<String> getAllPropertyAndRoomImages() {
    return new HashSet<>(propertyService.findAllPropertyRoomImageUrls());
  }

  private Set<String> extractPublicIdsFromUrls(Set<String> urls) {
    return urls.stream().map(cloudinaryService::extractPublicIdFromUrl).collect(Collectors.toSet());
  }

}
