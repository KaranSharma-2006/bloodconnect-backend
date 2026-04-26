package com.bloodconnect.bloodconnect.service;

import com.bloodconnect.bloodconnect.dto.DonorRatingResponseDto;
import com.bloodconnect.bloodconnect.dto.HospitalDonorRatingResponseDto;
import com.bloodconnect.bloodconnect.dto.RatingRequestDto;
import com.bloodconnect.bloodconnect.model.*;
import com.bloodconnect.bloodconnect.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DonorRatingService {

    private static final Logger logger = LoggerFactory.getLogger(DonorRatingService.class);

    private final DonorRatingRepository ratingRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final HospitalProfileRepository hospitalRepository;
    private final DonorProfileRepository donorRepository;
    private final UserRepository userRepository;

    public DonorRatingService(DonorRatingRepository ratingRepository,
                              BloodRequestRepository bloodRequestRepository,
                              HospitalProfileRepository hospitalRepository,
                              DonorProfileRepository donorRepository,
                              UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.hospitalRepository = hospitalRepository;
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
    }

    public String submitRating(String hospitalEmail, RatingRequestDto dto) {
        // Resolve hospittal from authenticated email
        User hospitalUser = userRepository.findByEmail(hospitalEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital user not found"));
        HospitalProfile hospital = hospitalRepository.findByUser(hospitalUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital profile not found"));

        // Validate blood request exists
        BloodRequest bloodRequest = bloodRequestRepository.findById(dto.getBloodRequestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood request not found"));

        // Ensure request is COMPLETED
        if (!"COMPLETED".equalsIgnoreCase(bloodRequest.getStatus())) {
            logger.warn("action=submit_rating_failed reason=not_completed request_id={} hospital=\"{}\"",
                    dto.getBloodRequestId(), hospitalEmail);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You can only rate a donor after the blood request is marked COMPLETED");
        }

        // Ensure this hospital made the request
        if (!bloodRequest.getHospital().getId().equals(hospital.getId())) {
            logger.warn("action=submit_rating_failed reason=wrong_hospital request_id={} hospital=\"{}\"",
                    dto.getBloodRequestId(), hospitalEmail);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only rate donors for your own blood requests");
        }

        // Enforce one rating per request
        if (ratingRepository.existsByBloodRequestAndHospital(bloodRequest, hospital)) {
            logger.warn("action=submit_rating_failed reason=already_rated request_id={} hospital=\"{}\"",
                    dto.getBloodRequestId(), hospitalEmail);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already rated this donor for this blood request");
        }

        // Validate rating value
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        DonorRating rating = new DonorRating(
                bloodRequest.getDonor(),
                hospital,
                bloodRequest,
                dto.getRating(),
                dto.getFeedback()
        );
        ratingRepository.save(rating);

        logger.info("action=submit_rating_success request_id={} donor_id={} hospital=\"{}\" rating={}",
                dto.getBloodRequestId(), bloodRequest.getDonor().getId(), hospitalEmail, dto.getRating());

        return "Rating submitted successfully";
    }

    public DonorRatingResponseDto getDonorRatingForDonor(String donorEmail) {
        User donorUser = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor user not found"));
        DonorProfile donor = donorRepository.findByUser(donorUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor profile not found"));

        Double avg = ratingRepository.findAverageRatingByDonorId(donor.getId());
        Long total = ratingRepository.countByDonorId(donor.getId());

        // Round average to 1 decimal place
        double roundedAvg = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        String message = roundedAvg >= 4.0
                ? "Keep up the great work!"
                : "Thank you for donating!";

        return new DonorRatingResponseDto(roundedAvg, total, message);
    }

    public HospitalDonorRatingResponseDto getDonorRatingForHospital(Long donorId) {
        DonorProfile donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));

        Double avg = ratingRepository.findAverageRatingByDonorId(donorId);
        double roundedAvg = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        long totalDonations = bloodRequestRepository.countByDonorIdAndStatus(donorId, "COMPLETED");

        return new HospitalDonorRatingResponseDto(
                donor.getId(),
                donor.getUser().getName(),
                donor.getBloodGroup(),
                roundedAvg,
                totalDonations
        );
    }
}
