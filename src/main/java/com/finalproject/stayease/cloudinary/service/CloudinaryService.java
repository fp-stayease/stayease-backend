package com.finalproject.stayease.cloudinary.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folderName) throws IOException;
}
