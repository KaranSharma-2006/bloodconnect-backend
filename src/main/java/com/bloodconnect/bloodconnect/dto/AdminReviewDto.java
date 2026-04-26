package com.bloodconnect.bloodconnect.dto;

import com.bloodconnect.bloodconnect.model.VerificationStatus;

public class AdminReviewDto {
    private Long verificationId;
    private VerificationStatus status;
    private String adminNote;

    public Long getVerificationId() { return verificationId; }
    public void setVerificationId(Long verificationId) { this.verificationId = verificationId; }

    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}
