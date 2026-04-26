package com.bloodconnect.bloodconnect.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    private static final Logger logger = Logger.getLogger(FileUploadController.class.getName());

    @GetMapping("/{filename}")
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        try {
            logger.info("=== FILE SERVING REQUEST ===");
            logger.info("Requested filename: " + filename);
            logger.info("Upload directory from config: " + uploadDir);
            
            // Prevent directory traversal attacks
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                logger.warning("Attempted directory traversal with filename: " + filename);
                return ResponseEntity.badRequest().body("Invalid filename");
            }

            // Use the configured upload directory directly
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            logger.info("Absolute base path: " + basePath.toString());
            
            Path filePath = basePath.resolve(filename).normalize();
            logger.info("Resolved file path: " + filePath.toString());
            
            // Ensure the resolved path is still within the upload directory
            if (!filePath.startsWith(basePath)) {
                logger.warning("Path escape detected for filename: " + filename);
                return ResponseEntity.badRequest().body("Invalid file path");
            }

            File file = filePath.toFile();
            logger.info("File object created: " + file.getAbsolutePath());
            logger.info("File exists: " + file.exists());
            logger.info("Is file: " + file.isFile());
            logger.info("File size: " + file.length());

            if (!file.exists() || !file.isFile()) {
                logger.warning("File not found or is not a file: " + filePath.toString());
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            String mediaType = getMediaType(filename);
            
            logger.info("Successfully serving file: " + filename + " with type: " + mediaType + ", size: " + fileContent.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(mediaType))
                    .body(fileContent);
        } catch (Exception e) {
            logger.severe("Error serving file '" + filename + "': " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error serving file: " + e.getMessage());
        }
    }

    private String getMediaType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}
