package com.finalproject.stayease.cloudinary.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folderName) throws IOException;
    List<String> findAllImagesFromFolder(String folderName);
    void deleteImage(String imageUrl) throws IOException;
}
