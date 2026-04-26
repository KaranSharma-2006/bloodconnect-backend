package com.bloodconnect.bloodconnect.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloodconnect.bloodconnect.dto.AdminBloodRequestResponse;
import com.bloodconnect.bloodconnect.dto.AdminDashboardResponse;
import com.bloodconnect.bloodconnect.dto.AdminUserDetailResponse;
import com.bloodconnect.bloodconnect.dto.AdminUserResponse;
import com.bloodconnect.bloodconnect.dto.AdminVerificationResponseDto;
import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.model.Role;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;
import com.bloodconnect.bloodconnect.service.AdminService;
import com.bloodconnect.bloodconnect.service.DonorVerificationService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminService adminService;
    private final DonorProfileRepository donorProfileRepository;
    private final DonorVerificationService donorVerificationService;

    public AdminController(UserRepository userRepository,
                           AdminService adminService,
                           DonorProfileRepository donorProfileRepository,
                           DonorVerificationService donorVerificationService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
        this.donorProfileRepository = donorProfileRepository;
        this.donorVerificationService = donorVerificationService;
    }

    // 👀 View all users except admins (SAFE DTO)
    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != Role.ROLE_ADMIN)
                .map(user -> {
                    String vStatus = "PENDING";
                    if (user.getRole() == Role.ROLE_DONOR && !user.isVerified()) {
                        DonorProfile donor = donorProfileRepository.findByUser(user).orElse(null);
                        if (donor != null) {
                            try {
                                AdminVerificationResponseDto v = donorVerificationService.getVerificationByDonorId(donor.getId());
                                vStatus = v.getStatus().name();
                            } catch (Exception e) {
                                vStatus = "PENDING";
                            }
                        }
                    } else if (user.isVerified()) {
                        vStatus = "APPROVED";
                    }
                    return new AdminUserResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole(),
                            user.isVerified(),
                            user.isBlocked(),
                            null, // city is not fetched here
                            vStatus
                    );
                })
                .toList();
    }

    // ✅ Verify user
    @PutMapping("/verify/{id}")
    public User verifyUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerified(true);

        return userRepository.save(user);
    }

    // 🚫 Block user
    @PutMapping("/block/{id}")
    public User blockUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equals(currentEmail)) {
            throw new RuntimeException("You cannot block your own account");
        }

        user.setBlocked(true);

        return userRepository.save(user);
    }

    // 🔓 Unblock user
    @PutMapping("/unblock/{id}")
    public User unblockUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBlocked(false);

        return userRepository.save(user);
    }

    // 📊 Dashboard statistics
    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return adminService.getDashboardStats();
    }

    // 🔍 View detailed user profile (excluding admins)
    @GetMapping("/user/{id}")
    public AdminUserDetailResponse getUserDetail(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new RuntimeException("Admin profiles cannot be viewed or managed");
        }
        
        return adminService.getUserDetail(id);
    }

    // 🆔 Get verification details for a donor by user ID
    @GetMapping("/user/{id}/verification")
    public ResponseEntity<AdminVerificationResponseDto> getUserVerification(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != Role.ROLE_DONOR) {
            throw new RuntimeException("Verification is only available for donors");
        }

        DonorProfile donor = donorProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));

        AdminVerificationResponseDto verification = donorVerificationService.getVerificationByDonorId(donor.getId());
        return ResponseEntity.ok(verification);
    }

    // 📋 Get all blood requests in the system
    @GetMapping("/all-requests")
    public List<AdminBloodRequestResponse> getAllBloodRequests() {
        return adminService.getAllBloodRequests();
    }
}