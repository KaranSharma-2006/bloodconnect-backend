package com.bloodconnect.bloodconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bloodconnect.bloodconnect.dto.RegisterRequest;
import com.bloodconnect.bloodconnect.dto.UserResponse;
import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.model.HospitalProfile;
import com.bloodconnect.bloodconnect.model.Role;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.bloodconnect.repository.HospitalProfileRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DonorProfileRepository donorRepository;
    private final HospitalProfileRepository hospitalRepository;
    private final DonorVerificationService donorVerificationService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       DonorProfileRepository donorRepository,
                       HospitalProfileRepository hospitalRepository,
                       DonorVerificationService donorVerificationService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.donorRepository = donorRepository;
        this.hospitalRepository = hospitalRepository;
        this.donorVerificationService = donorVerificationService;
        this.emailService = emailService;
    }

    /**
     * Legacy register method for backward compatibility. Does NOT trigger verification.
     * Use registerWithVerification() for donor registration going forward.
     */
    public UserResponse register(RegisterRequest request) {
        return registerWithVerification(request, null).equals("") ? null : null;
    }

    /**
     * Main registration method. For donors, accepts a document file and submits it for
     * admin verification. Document verification is part of the registration flow.
     */
    public String registerWithVerification(RegisterRequest request, MultipartFile documentFile) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Email already in use"
            );
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                false,
                false
        );

        User saved = userRepository.save(user);
        logger.info("action=register_user email={} role={}", saved.getEmail(), saved.getRole());

        if (saved.getRole() == Role.ROLE_DONOR) {
            try {
                DonorProfile donor = new DonorProfile();
                donor.setBloodGroup(request.getBloodGroup());
                donor.setPhone(request.getPhone());
                donor.setCity(request.getCity());
                donor.setState(request.getState());
                donor.setArea(request.getArea());
                donor.setAddress(request.getAddress());
                donor.setPincode(request.getPincode());
                donor.setLatitude(request.getLatitude());
                donor.setLongitude(request.getLongitude());
                donor.setAvailable(true);
                donor.setVerified(false); // Starts unverified
                donor.setUser(saved);
                donorRepository.save(donor);

                // Step 1 & 2: Submit verification (uploads to Cloudinary internally + saves record)
                logger.info("action=auto_submit_verification email={} docType={}", saved.getEmail(), request.getDocumentType());
                donorVerificationService.submitVerification(saved.getEmail(), documentFile, request.getDocumentType());

                // Step 3: Send welcome + pending review email
                String subject = "Welcome to Blood Connect – Verification Pending";
                String body = String.format(
                    "Dear %s,<br><br>" +
                    "Thank you for registering on Blood Connect!<br><br>" +
                    "Your <b>%s</b> has been submitted for verification. " +
                    "Our admin team will review it shortly.<br><br>" +
                    "You will receive an email once your account is approved.<br><br>" +
                    "Blood Connect Team",
                    saved.getName(), request.getDocumentType()
                );
                try {
                    emailService.sendEmail(saved.getEmail(), subject, body);
                    logger.info("action=registration_welcome_email_sent email={}", saved.getEmail());
                } catch (Exception emailEx) {
                    logger.warn("action=registration_welcome_email_failed email={} error={}", saved.getEmail(), emailEx.getMessage());
                    // Don't fail registration if email fails - email is non-critical
                }

                // Step 4: Return pending message
                return "Registration successful! Your document is under review. You will receive an email once your account is verified.";
                
            } catch (Exception ex) {
                logger.error("action=donor_registration_failed email={} error={}", saved.getEmail(), ex.getMessage(), ex);
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Registration failed: " + ex.getMessage()
                );
            }
        } else if (saved.getRole() == Role.ROLE_HOSPITAL) {
            HospitalProfile hospital = new HospitalProfile();
            hospital.setHospitalName(request.getName());
            hospital.setLicenseNumber(request.getLicenseNumber());
            hospital.setCity(request.getLocation());
            hospital.setState(request.getState());
            hospital.setArea(request.getArea());
            hospital.setAddress(request.getAddress());
            hospital.setPincode(request.getPincode());
            hospital.setLatitude(request.getLatitude());
            hospital.setLongitude(request.getLongitude());
            hospital.setContactNumber(request.getPhone());
            hospital.setVerified(false);
            hospital.setUser(saved);
            hospitalRepository.save(hospital);
        }

        return "Registration successful!";
    }
}
