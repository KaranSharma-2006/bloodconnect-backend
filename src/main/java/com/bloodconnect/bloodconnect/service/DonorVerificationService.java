package com.bloodconnect.bloodconnect.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.bloodconnect.bloodconnect.dto.AdminReviewDto;
import com.bloodconnect.bloodconnect.dto.AdminVerificationResponseDto;
import com.bloodconnect.bloodconnect.dto.VerificationResponseDto;
import com.bloodconnect.bloodconnect.model.DocumentType;
import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.model.DonorVerification;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.model.VerificationStatus;
import com.bloodconnect.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.bloodconnect.repository.DonorVerificationRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;

@Service
public class DonorVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(DonorVerificationService.class);

    private final DonorVerificationRepository verificationRepository;
    private final DonorProfileRepository donorRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    public DonorVerificationService(DonorVerificationRepository verificationRepository,
                                    DonorProfileRepository donorRepository,
                                    UserRepository userRepository,
                                    CloudinaryService cloudinaryService,
                                    EmailService emailService) {
        this.verificationRepository = verificationRepository;
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService;
    }

    @Transactional
    public String submitVerification(String donorEmail, MultipartFile file, String documentType) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        DonorProfile donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor profile not found"));

        // Check for active (PENDING or APPROVED) verification
        boolean hasActive = verificationRepository.existsByDonorIdAndStatusIn(
                donor.getId(), Arrays.asList(VerificationStatus.PENDING, VerificationStatus.APPROVED));
        
        if (hasActive) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already have a pending or approved verification");
        }

        // Upload to Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file);

        // Save verification
        DonorVerification verification = new DonorVerification();
        verification.setDonor(donor);
        verification.setDocumentType(DocumentType.valueOf(documentType.toUpperCase()));
        verification.setDocumentImageUrl(imageUrl);
        verification.setStatus(VerificationStatus.PENDING);
        verificationRepository.save(verification);

        // Send Email
        String subject = "Verification Submitted – Blood Connect";
        String body = String.format("Dear %s, your %s has been submitted for verification. We will review it shortly.", 
                                    user.getName(), documentType);
        emailService.sendEmail(donorEmail, subject, body);

        logger.info("action=submit_verification donor_id={} status=PENDING", donor.getId());
        return "Verification submitted successfully. Pending admin review.";
    }

    public VerificationResponseDto getMyVerificationStatus(String donorEmail) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        DonorProfile donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor profile not found"));

        DonorVerification latest = verificationRepository.findFirstByDonorIdOrderBySubmittedAtDesc(donor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No verification found"));

        return new VerificationResponseDto(
                latest.getDocumentType(),
                latest.getStatus(),
                latest.getStatus() == VerificationStatus.REJECTED ? latest.getAdminNote() : null,
                latest.getSubmittedAt()
        );
    }

    public List<AdminVerificationResponseDto> getPendingVerifications() {
        return verificationRepository.findByStatus(VerificationStatus.PENDING).stream()
                .map(v -> new AdminVerificationResponseDto(
                        v.getId(),
                        v.getDonor().getId(),
                        v.getDonor().getUser().getName(),
                        v.getDonor().getUser().getEmail(),
                        v.getDocumentType(),
                        v.getDocumentImageUrl(),
                        v.getStatus(),
                        v.getSubmittedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void reviewVerification(AdminReviewDto dto) {
        DonorVerification verification = verificationRepository.findById(dto.getVerificationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification not found"));

        if (dto.getStatus() == VerificationStatus.REJECTED && (dto.getAdminNote() == null || dto.getAdminNote().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin note is required when rejecting");
        }

        verification.setStatus(dto.getStatus());
        verification.setAdminNote(dto.getAdminNote());
        verification.setReviewedAt(LocalDateTime.now());
        verificationRepository.save(verification);

        // Update DonorProfile verified status
        DonorProfile donor = verification.getDonor();
        donor.setVerified(dto.getStatus() == VerificationStatus.APPROVED);
        donorRepository.save(donor);

        // Send Email
        String donorEmail = donor.getUser().getEmail();
        String donorName = donor.getUser().getName();
        String subject, body;

        if (dto.getStatus() == VerificationStatus.APPROVED) {
            subject = "Identity Verified – Blood Connect";
            body = String.format("Dear %s, your identity has been successfully verified. You now have a verified badge on your profile. Thank you!", donorName);
        } else {
            subject = "Verification Rejected – Blood Connect";
            body = String.format("Dear %s, your %s verification was rejected. Reason: %s. Please resubmit with a clear document image.", 
                                 donorName, verification.getDocumentType(), dto.getAdminNote());
        }

        emailService.sendEmail(donorEmail, subject, body);
        logger.info("action=review_verification verification_id={} new_status={}", verification.getId(), dto.getStatus());
    }

    public AdminVerificationResponseDto getVerificationByDonorId(Long donorId) {
        DonorVerification latest = verificationRepository.findFirstByDonorIdOrderBySubmittedAtDesc(donorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No verification found for this donor"));

        return new AdminVerificationResponseDto(
                latest.getId(),
                latest.getDonor().getId(),
                latest.getDonor().getUser().getName(),
                latest.getDonor().getUser().getEmail(),
                latest.getDocumentType(),
                latest.getDocumentImageUrl(),
                latest.getStatus(),
                latest.getSubmittedAt()
        );
    }
}
