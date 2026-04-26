package com.bloodconnect.bloodconnect.service;

import com.bloodconnect.bloodconnect.dto.HospitalProfileRequest;
import com.bloodconnect.bloodconnect.model.HospitalProfile;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.HospitalProfileRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class HospitalProfileService {

    private final HospitalProfileRepository hospitalRepository;
    private final UserRepository userRepository;

    public HospitalProfileService(HospitalProfileRepository hospitalRepository,
                                  UserRepository userRepository) {
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
    }

    public HospitalProfile getProfile(String email) {
        return hospitalRepository.findByUserEmail(email).orElse(null);
    }

    public HospitalProfile createOrUpdateProfile(String email, HospitalProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HospitalProfile profile = hospitalRepository.findByUser(user).orElse(null);

        if (profile == null) {
            profile = new HospitalProfile();
            profile.setUser(user);
        }

        profile.setHospitalName(request.getHospitalName());
        profile.setLicenseNumber(request.getLicenseNumber());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setArea(request.getArea());
        profile.setAddress(request.getAddress());
        profile.setPincode(request.getPincode());
        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());

        if (profile.isVerified() == false) {
            profile.setVerified(false);
        }

        return hospitalRepository.save(profile);
    }

    /** @deprecated use createOrUpdateProfile */
    public HospitalProfile createProfile(String email, HospitalProfileRequest request) {
        return createOrUpdateProfile(email, request);
    }
}