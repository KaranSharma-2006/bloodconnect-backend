package com.bloodconnect.bloodconnect.dto;

import org.springframework.web.multipart.MultipartFile;
import com.bloodconnect.bloodconnect.model.DocumentType;

public class VerificationSubmitDto {
    private DocumentType documentType;
    private MultipartFile file;

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
}
