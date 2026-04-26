package com.bloodconnect.bloodconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminReplyDto {
    
    @NotNull(message = "Complaint ID is required")
    private Long complaintId;

    @NotBlank(message = "Reply is required")
    private String reply;

    public AdminReplyDto() {}

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
