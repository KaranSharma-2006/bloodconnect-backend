package com.bloodconnect.bloodconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                            @Value("${cloudinary.api-key}") String apiKey,
                            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String uploadImage(MultipartFile file) {
        try {
            logger.info("action=upload_image filename=\"{}\" size={}", file.getOriginalFilename(), file.getSize());
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "blood-connect/verifications"
            ));
            String url = (String) uploadResult.get("secure_url");
            logger.info("action=upload_image_success url=\"{}\"", url);
            return url;
        } catch (IOException e) {
            logger.error("action=upload_image_failed error=\"{}\"", e.getMessage());
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
        }
    }
}
