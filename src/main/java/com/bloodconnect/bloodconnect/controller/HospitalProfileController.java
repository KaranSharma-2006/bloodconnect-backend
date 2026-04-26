package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.dto.HospitalProfileRequest;
import com.bloodconnect.bloodconnect.model.HospitalProfile;
import com.bloodconnect.bloodconnect.service.HospitalProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospital")
public class HospitalProfileController {

    private final HospitalProfileService hospitalService;

    public HospitalProfileController(HospitalProfileService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        HospitalProfile profile = hospitalService.getProfile(email);
        if (profile == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @PostMapping("/profile")
    public HospitalProfile createOrUpdateProfile(@RequestBody HospitalProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return hospitalService.createOrUpdateProfile(email, request);
    }
}