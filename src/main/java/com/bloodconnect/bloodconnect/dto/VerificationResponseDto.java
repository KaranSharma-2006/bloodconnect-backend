package com.bloodconnect.bloodconnect.dto;

import com.bloodconnect.bloodconnect.model.VerificationStatus;
import com.bloodconnect.bloodconnect.model.DocumentType;
import java.time.LocalDateTime;

public class VerificationResponseDto {
    private DocumentType documentType;
    private VerificationStatus status;
    private String adminNote;
    private LocalDateTime submittedAt;

    public VerificationResponseDto(DocumentType documentType, VerificationStatus status, String adminNote, LocalDateTime submittedAt) {
        this.documentType = documentType;
        this.status = status;
        this.adminNote = adminNote;
        this.submittedAt = submittedAt;
    }

    // Getters
    public DocumentType getDocumentType() { return documentType; }
    public VerificationStatus getStatus() { return status; }
    public String getAdminNote() { return adminNote; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
