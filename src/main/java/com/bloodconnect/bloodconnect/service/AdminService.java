package com.bloodconnect.bloodconnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bloodconnect.bloodconnect.dto.AdminBloodRequestResponse;
import com.bloodconnect.bloodconnect.dto.AdminDashboardResponse;
import com.bloodconnect.bloodconnect.dto.AdminUserDetailResponse;
import com.bloodconnect.bloodconnect.model.Role;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.BloodRequestRepository;
import com.bloodconnect.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.bloodconnect.repository.HospitalProfileRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final BloodRequestRepository requestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final HospitalProfileRepository hospitalProfileRepository;

    public AdminService(UserRepository userRepository,
                        BloodRequestRepository requestRepository,
                        DonorProfileRepository donorProfileRepository,
                        HospitalProfileRepository hospitalProfileRepository) {

        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.hospitalProfileRepository = hospitalProfileRepository;
    }

    public AdminDashboardResponse getDashboardStats() {

        long totalDonors = userRepository.countByRole(Role.ROLE_DONOR);
        long totalHospitals = userRepository.countByRole(Role.ROLE_HOSPITAL);
        long activeUsers = userRepository.countByVerifiedTrue();
        long pendingVerification = userRepository.countByVerifiedFalseAndRoleNot(Role.ROLE_ADMIN);

        long totalBloodRequests = requestRepository.count();
        long pendingBloodRequests = requestRepository.countByStatus("PENDING");

        return new AdminDashboardResponse(
                totalDonors,
                totalHospitals,
                activeUsers,
                pendingVerification,
                totalBloodRequests,
                pendingBloodRequests
        );
    }

    public AdminUserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminUserDetailResponse detail = new AdminUserDetailResponse();
        detail.setId(user.getId());
        detail.setName(user.getName());
        detail.setEmail(user.getEmail());
        detail.setRole(user.getRole());
        detail.setVerified(user.isVerified());
        detail.setBlocked(user.isBlocked());

        if (user.getRole() == Role.ROLE_DONOR) {
            donorProfileRepository.findByUser(user).ifPresent(p -> {
                detail.setBloodGroup(p.getBloodGroup());
                detail.setPhone(p.getPhone());
                detail.setCity(p.getCity());
                detail.setAvailable(p.isAvailable());
                detail.setLastDonationDate(p.getLastDonationDate());
            });
        } else if (user.getRole() == Role.ROLE_HOSPITAL) {
            hospitalProfileRepository.findByUser(user).ifPresent(p -> {
                detail.setHospitalName(p.getHospitalName());
                detail.setLicenseNumber(p.getLicenseNumber());
                detail.setCity(p.getCity());
            });
        }

        return detail;
    }

    public List<AdminBloodRequestResponse> getAllBloodRequests() {
        return requestRepository.findAll()
                .stream()
                .map(request -> new AdminBloodRequestResponse(
                        request.getId(),
                        request.getHospital().getUser().getId(),
                        request.getDonor().getUser().getId(),
                        request.getStatus(),
                        request.getRequestTime(),
                        request.isEmergency()
                ))
                .toList();
    }
}