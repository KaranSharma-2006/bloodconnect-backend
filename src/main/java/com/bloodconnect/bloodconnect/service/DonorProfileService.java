package com.bloodconnect.bloodconnect.service;

import com.bloodconnect.bloodconnect.dto.DonorProfileRequest;
import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class DonorProfileService {

    private final DonorProfileRepository donorRepository;
    private final UserRepository userRepository;

    public DonorProfileService(DonorProfileRepository donorRepository,
                               UserRepository userRepository) {
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
    }

    public DonorProfile getProfile(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;
        return donorRepository.findByUser(user).orElse(null);
    }

    public DonorProfile createOrUpdateProfile(String email, DonorProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DonorProfile profile = donorRepository.findByUser(user).orElse(null);

        if (profile == null) {
            profile = new DonorProfile(
                    request.getBloodGroup(),
                    request.getPhone(),
                    request.getCity(),
                    request.getState(),
                    request.getArea(),
                    request.getAddress(),
                    request.getPincode(),
                    true,
                    null,
                    user
            );
        } else {
            profile.setBloodGroup(request.getBloodGroup());
            profile.setPhone(request.getPhone());
            profile.setCity(request.getCity());
            profile.setState(request.getState());
            profile.setArea(request.getArea());
            profile.setAddress(request.getAddress());
            profile.setPincode(request.getPincode());
        }

        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());

        return donorRepository.save(profile);
    }

    /** @deprecated use createOrUpdateProfile */
    public DonorProfile createProfile(String email, DonorProfileRequest request) {
        return createOrUpdateProfile(email, request);
    }
}