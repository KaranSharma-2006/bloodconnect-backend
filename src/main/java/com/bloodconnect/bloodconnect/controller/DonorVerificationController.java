package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.dto.*;
import com.bloodconnect.bloodconnect.service.DonorVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/verification")
public class DonorVerificationController {

    private final DonorVerificationService verificationService;

    public DonorVerificationController(DonorVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<String> submitVerification(Principal principal,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam("documentType") String documentType) {
        String message = verificationService.submitVerification(principal.getName(), file, documentType);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/my-status")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<VerificationResponseDto> getMyStatus(Principal principal) {
        return ResponseEntity.ok(verificationService.getMyVerificationStatus(principal.getName()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AdminVerificationResponseDto>> getPending() {
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    @PutMapping("/review")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> review(@RequestBody AdminReviewDto dto) {
        verificationService.reviewVerification(dto);
        return ResponseEntity.ok("Verification status updated successfully.");
    }
}
