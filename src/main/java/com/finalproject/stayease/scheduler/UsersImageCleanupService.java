package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class UsersImageCleanupService {

  private final CloudinaryService cloudinaryService;
  private final UsersService usersService;

  @SneakyThrows
  @Scheduled(cron = "${cron.cleanup.cloudinary:0 0 * * * ?}")

  public void cleanupOrphanedImages() {
    log.info("Cleaning up users' orphaned images...");

    Set<String> allUsersImagesPublicIds = extractPublicIdsFromUrls(getAllUsersImages());
    log.info("All users images public IDs: {}", allUsersImagesPublicIds);
    Set<String> allImagesInCloudinaryPublicIds =
        extractPublicIdsFromUrls(new HashSet<>(cloudinaryService.findAllImagesFromFolder("users/")));
    log.info("All images in User's Folder Cloudinary public IDs: {}", allImagesInCloudinaryPublicIds == null ? "null" : allImagesInCloudinaryPublicIds.isEmpty() ? "empty" : allImagesInCloudinaryPublicIds);

    assert allImagesInCloudinaryPublicIds != null;
    allImagesInCloudinaryPublicIds.removeAll(allUsersImagesPublicIds);
    log.info("Orphaned images in User's Folder Cloudinary public IDs: {}",
        allImagesInCloudinaryPublicIds.isEmpty() ? "empty" : allImagesInCloudinaryPublicIds);

    for (String imageUrl : allImagesInCloudinaryPublicIds) {
      cloudinaryService.deleteImage(imageUrl);
    }
  }

  private Set<String> getAllUsersImages() {
    List<String> allUsersAvatars = usersService.findAllAvatars()
        .stream().filter(avatar -> avatar != null && !avatar.isEmpty())
        .filter(avatar -> avatar.contains("res.cloudinary.com")).collect(Collectors.toList());
    log.info("All users avatars: {}", allUsersAvatars);
    return new HashSet<>(allUsersAvatars);
  }

  private Set<String> extractPublicIdsFromUrls(Set<String> urls) {
    return urls.stream().map(cloudinaryService::extractPublicIdFromUrl).collect(Collectors.toSet());
  }

}
