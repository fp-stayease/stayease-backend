package com.finalproject.stayease.cloudinary.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

  private final Cloudinary cloudinary;
  private static final Pattern PUBLIC_ID_PATTERN = Pattern.compile("/v\\d+/(.+?)(?:\\.[^.]+)?$");

  public CloudinaryServiceImpl(Cloudinary cloudinary) {
    this.cloudinary = cloudinary;
  }

  @Override
  public String uploadFile(MultipartFile file, String folderName) throws IOException {
    HashMap<Object, Object> options = new HashMap<>();
    options.put("folder", folderName);
    log.info("Uploading file: {} to folder: {}", file.getOriginalFilename(), folderName);
    Map uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
    String publicId = (String) uploadedFile.get("public_id");

    return cloudinary.url().secure(true).generate(publicId);
  }

  @Override
  public List<String> findAllImagesFromFolder(String folderName) {
    log.info("Retrieving images from folder: {}", folderName);
    List<String> imageUrls = new ArrayList<>();
    String nextCursor = null;

    try {
      log.info("Entering try block to retrieve images from folder: {}", folderName);
      do {
        log.info("Retrieving images with cursor: {}", nextCursor);
        Map<String, Object> params = new HashMap<>();
        params.put("type", "upload");
        params.put("max_results", 500);
        params.put("prefix", folderName.endsWith("/") ? folderName : folderName + "/");
        if (nextCursor != null) {
          params.put("next_cursor", nextCursor);
          log.info("Next cursor: {}", nextCursor);
        }

        log.debug("Cloudinary API request params: {}", params);
        ApiResponse response = cloudinary.api().resources(params);
        log.debug("Cloudinary API response: {}", response);

        List<Map<String, Object>> resources = (List<Map<String, Object>>) response.get("resources");
        log.info("Found {} resources in this batch", resources.size());

        for (Map<String, Object> resource : resources) {
          String url = (String) resource.get("url");
          log.info("Found image: {}", url);
          imageUrls.add(url);
        }

        nextCursor = (String) response.get("next_cursor");
      } while (nextCursor != null);
    } catch (Exception e) {
      // TODO : make exception handling more specific ImageRetrievalException
      throw new RuntimeException("Failed to retrieve images from folder: " + folderName, e);
    }

    log.info("Found {} images in folder: {}", imageUrls.size(), folderName);
    return imageUrls;
  }

  @Override
  public void deleteImage(String imagePublicId) throws IOException {
    log.info("Attempting to delete image with public ID: {}", imagePublicId);
    try {
      Map result = cloudinary.uploader().destroy(imagePublicId, ObjectUtils.emptyMap());
      if ("ok".equals(result.get("result"))) {
        log.info("Successfully deleted image: {}", imagePublicId);
      } else {
        log.warn("Unexpected result when deleting image: {}", result);
      }
    } catch (Exception e) {
      log.error("Failed to delete image: {}", imagePublicId, e);
      throw new IOException("Failed to delete image: " + imagePublicId, e);
    }
  }

  @Override
  public String extractPublicIdFromUrl(String imageUrl) {
    Matcher matcher = PUBLIC_ID_PATTERN.matcher(imageUrl);
    if (matcher.find()) {
      return matcher.group(1);
    }
    log.warn("Could not extract public ID from URL: {}", imageUrl);
    throw new IllegalArgumentException("Invalid Cloudinary URL format");
  }
}
