package com.bloodconnect.bloodconnect.config;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    private static final Logger logger = Logger.getLogger(WebConfig.class.getName());

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convert Windows path to proper file URL format
        String fileUrl = "file:///" + uploadDir.replace("\\", "/");
        logger.info("Configuring resource handler for /uploads/** -> " + fileUrl);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUrl + "/")
                .setCachePeriod(3600);
    }
}
