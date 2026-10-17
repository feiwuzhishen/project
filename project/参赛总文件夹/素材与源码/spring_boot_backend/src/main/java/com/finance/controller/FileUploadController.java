package com.finance.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("error", "请选择要上传的文件");
                return result;
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("error", "只能上传图片文件");
                return result;
            }

            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                result.put("success", false);
                result.put("error", "文件大小不能超过5MB");
                return result;
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String newFilename = "avatar_" + userId + "_" + System.currentTimeMillis() + extension;
            
            Path uploadPath = Paths.get(uploadDir, "avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String avatarUrl = "/uploads/avatars/" + newFilename;
            
            result.put("success", true);
            result.put("avatarUrl", avatarUrl);
            result.put("message", "头像上传成功");
        } catch (IOException e) {
            result.put("success", false);
            result.put("error", "文件上传失败: " + e.getMessage());
        }
        
        return result;
    }

    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("error", "请选择要上传的文件");
                return result;
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("error", "只能上传图片文件");
                return result;
            }

            long maxSize = 10 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                result.put("success", false);
                result.put("error", "文件大小不能超过10MB");
                return result;
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String newFilename = "img_" + System.currentTimeMillis() + extension;
            
            Path uploadPath = Paths.get(uploadDir, "images");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            String imageUrl = "/uploads/images/" + newFilename;
            
            result.put("success", true);
            result.put("imageUrl", imageUrl);
            result.put("message", "图片上传成功");
        } catch (IOException e) {
            result.put("success", false);
            result.put("error", "文件上传失败: " + e.getMessage());
        }
        
        return result;
    }
}
