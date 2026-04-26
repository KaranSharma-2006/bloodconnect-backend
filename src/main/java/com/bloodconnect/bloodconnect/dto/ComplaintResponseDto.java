package com.bloodconnect.bloodconnect.dto;

import java.time.LocalDateTime;

public class ComplaintResponseDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private String adminReply;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public ComplaintResponseDto(Long id, String title, String description, String category, String status, String adminReply, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.adminReply = adminReply;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getAdminReply() { return adminReply; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
