package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.users.service.UsersService;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class UsersImageCleanupService {

  private final CloudinaryService cloudinaryService;
  private final UsersService usersService;

  @SneakyThrows
  @Scheduled(cron = "${cron.cleanup.cloudinary:0 0 * * * ?}")
  public void cleanupOrphanedImages() {
    Set<String> allUsersImagesPublicIds = extractPublicIdsFromUrls(getAllUsersImages());
    Set<String> allImagesInCloudinaryPublicIds =
        extractPublicIdsFromUrls(new HashSet<>(cloudinaryService.findAllImagesFromFolder("users/")));

    allImagesInCloudinaryPublicIds.removeAll(allUsersImagesPublicIds);

    for (String imageUrl : allImagesInCloudinaryPublicIds) {
      cloudinaryService.deleteImage(imageUrl);
    }
  }

  private Set<String> getAllUsersImages() {
    return new HashSet<>(usersService.findAllAvatars());
  }

  private Set<String> extractPublicIdsFromUrls(Set<String> urls) {
    return urls.stream().map(cloudinaryService::extractPublicIdFromUrl).collect(Collectors.toSet());
  }

}
