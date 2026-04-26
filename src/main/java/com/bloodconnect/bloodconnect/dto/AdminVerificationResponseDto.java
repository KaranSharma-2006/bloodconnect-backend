package com.bloodconnect.bloodconnect.dto;

import com.bloodconnect.bloodconnect.model.VerificationStatus;
import com.bloodconnect.bloodconnect.model.DocumentType;
import java.time.LocalDateTime;

public class AdminVerificationResponseDto {
    private Long verificationId;
    private Long donorId;
    private String donorName;
    private String donorEmail;
    private DocumentType documentType;
    private String documentImageUrl;
    private VerificationStatus status;
    private LocalDateTime submittedAt;

    public AdminVerificationResponseDto(Long verificationId, Long donorId, String donorName, String donorEmail, 
                                        DocumentType documentType, String documentImageUrl, VerificationStatus status, LocalDateTime submittedAt) {
        this.verificationId = verificationId;
        this.donorId = donorId;
        this.donorName = donorName;
        this.donorEmail = donorEmail;
        this.documentType = documentType;
        this.documentImageUrl = documentImageUrl;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    // Getters
    public Long getVerificationId() { return verificationId; }
    public Long getDonorId() { return donorId; }
    public String getDonorName() { return donorName; }
    public String getDonorEmail() { return donorEmail; }
    public DocumentType getDocumentType() { return documentType; }
    public String getDocumentImageUrl() { return documentImageUrl; }
    public VerificationStatus getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
