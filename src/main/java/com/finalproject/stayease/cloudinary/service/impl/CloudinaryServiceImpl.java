package com.finalproject.stayease.cloudinary.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import java.util.ArrayList;
import java.util.List;
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
    List<String> imageUrls = new ArrayList<>();
    String nextCursor = null;

    try {
      do {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "upload");
        params.put("max_results", 500);
        params.put("prefix", folderName);
        if (nextCursor != null) {
          params.put("next_cursor", nextCursor);
        }

        ApiResponse response = cloudinary.api().resources(params);

        List<Map<String, Object>> resources = (List<Map<String, Object>>) response.get("resources");

        for (Map<String, Object> resource : resources) {
          String url = (String) resource.get("url");
          imageUrls.add(url);
        }

        nextCursor = (String) response.get("next_cursor");
      } while (nextCursor != null);
    } catch (Exception e) {
      // TODO : make exception handling more specific ImageRetrievalException
      throw new RuntimeException("Failed to retrieve images from folder: " + folderName, e);
    }

    return imageUrls;
  }

  @Override
  public void deleteImage(String imageUrl) throws IOException {
    String publicId = extractPublicIdFromUrl(imageUrl);
    Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    if (!"ok".equals(result.get("result"))) {
      throw new IOException("Failed to delete image: " + imageUrl);
    }
  }

  private String extractPublicIdFromUrl(String imageUrl) {
    return imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
  }
}
