package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.dto.DonorSearchResponse;
import com.bloodconnect.bloodconnect.service.DonorService;
import com.bloodconnect.bloodconnect.util.SearchRateLimiter;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;
    private final SearchRateLimiter rateLimiter;
    private final UserRepository userRepository;

    public DonorController(DonorService donorService, SearchRateLimiter rateLimiter, UserRepository userRepository) {
        this.donorService = donorService;
        this.rateLimiter = rateLimiter;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @GetMapping("/search")
    public ResponseEntity<?> searchDonors(
            @RequestParam String bloodGroup,
            @RequestParam(required = false) Double radius,
            @RequestParam(defaultValue = "false") boolean includeUnverified,
            HttpServletRequest request) {

        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && !rateLimiter.isAllowed(user.getId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Search limit exceeded. Please try again in a minute.");
        }

        List<DonorSearchResponse> results = donorService.searchDonors(bloodGroup, radius, email, includeUnverified);
        return ResponseEntity.ok(results);
    }
}