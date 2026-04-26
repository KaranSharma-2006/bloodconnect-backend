package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.dto.DonorProfileRequest;
import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.service.DonorProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("/donor")
public class DonorProfileController {

    private final DonorProfileService donorService;

    public DonorProfileController(DonorProfileService donorService) {
        this.donorService = donorService;
    }

    @PreAuthorize("hasRole('DONOR')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DonorProfile profile = donorService.getProfile(email);
        if (profile == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("hasRole('DONOR')")
    @PostMapping("/profile")
    public DonorProfile createOrUpdateProfile(@RequestBody DonorProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return donorService.createOrUpdateProfile(email, request);
    }
}