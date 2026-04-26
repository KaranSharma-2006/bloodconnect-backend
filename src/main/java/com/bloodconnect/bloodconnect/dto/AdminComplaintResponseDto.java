package com.bloodconnect.bloodconnect.dto;

import java.time.LocalDateTime;

public class AdminComplaintResponseDto {
    private Long id;
    private String submittedBy;
    private String role;
    private String title;
    private String description;
    private String category;
    private String status;
    private String adminReply;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public AdminComplaintResponseDto(Long id, String submittedBy, String role, String title, String description, String category, String status, String adminReply, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.submittedBy = submittedBy;
        this.role = role;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.adminReply = adminReply;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getSubmittedBy() { return submittedBy; }
    public String getRole() { return role; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getAdminReply() { return adminReply; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
