package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.dto.DonorRatingResponseDto;
import com.bloodconnect.bloodconnect.dto.HospitalDonorRatingResponseDto;
import com.bloodconnect.bloodconnect.dto.RatingRequestDto;
import com.bloodconnect.bloodconnect.service.DonorRatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class DonorRatingController {

    private final DonorRatingService donorRatingService;

    public DonorRatingController(DonorRatingService donorRatingService) {
        this.donorRatingService = donorRatingService;
    }

    /**
     * POST /api/ratings/submit
     * Accessible by HOSPITAL role only.
     * Submits a rating for a donor after a completed blood request.
     */
    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @PostMapping("/submit")
    public ResponseEntity<String> submitRating(@RequestBody RatingRequestDto dto) {
        String hospitalEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String result = donorRatingService.submitRating(hospitalEmail, dto);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/ratings/my-rating
     * Accessible by DONOR role only.
     * Returns donor's average rating and total count. Hospital identity is never exposed.
     */
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    @GetMapping("/my-rating")
    public ResponseEntity<DonorRatingResponseDto> getMyRating() {
        String donorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        DonorRatingResponseDto response = donorRatingService.getDonorRatingForDonor(donorEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/ratings/donor/{donorId}
     * Accessible by HOSPITAL role only.
     * Returns a donor's name, blood group, average rating and total donations.
     */
    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @GetMapping("/donor/{donorId}")
    public ResponseEntity<HospitalDonorRatingResponseDto> getDonorRating(@PathVariable Long donorId) {
        HospitalDonorRatingResponseDto response = donorRatingService.getDonorRatingForHospital(donorId);
        return ResponseEntity.ok(response);
    }
}
