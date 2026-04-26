package com.bloodconnect.bloodconnect.service;

import com.bloodconnect.bloodconnect.dto.BloodRequestResponse;
import com.bloodconnect.bloodconnect.dto.BloodRequestPayload;
import com.bloodconnect.bloodconnect.model.*;
import com.bloodconnect.bloodconnect.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BloodRequestService {

    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    private final BloodRequestRepository requestRepository;
    private final HospitalProfileRepository hospitalRepository;
    private final DonorProfileRepository donorRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final RateLimitingService rateLimitingService;

    public BloodRequestService(BloodRequestRepository requestRepository,
                               HospitalProfileRepository hospitalRepository,
                               DonorProfileRepository donorRepository,
                               UserRepository userRepository,
                               NotificationService notificationService,
                               EmailService emailService,
                               RateLimitingService rateLimitingService) {
        this.requestRepository = requestRepository;
        this.hospitalRepository = hospitalRepository;
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.rateLimitingService = rateLimitingService;
    }

    private BloodRequestResponse mapToResponse(BloodRequest request) {
        String phone = null;
        String email = null;

        if ("ACCEPTED".equalsIgnoreCase(request.getStatus())) {
            phone = request.getDonor().getPhone();
            email = request.getDonor().getUser().getEmail();
        }

        Double distance = null;
        DonorProfile donor = request.getDonor();
        HospitalProfile hospital = request.getHospital();
        if (donor.getLatitude() != null && donor.getLongitude() != null && 
            hospital.getLatitude() != null && hospital.getLongitude() != null) {
            distance = calculateDistance(donor.getLatitude(), donor.getLongitude(), 
                                         hospital.getLatitude(), hospital.getLongitude());
            distance = Math.round(distance * 10.0) / 10.0;
        }

        return new BloodRequestResponse(
                request.getId(),
                request.getDonor().getId(),
                request.getHospital().getHospitalName(),
                request.getDonor().getUser().getName(),
                request.getDonor().getBloodGroup(),
                request.getStatus(),
                request.getRequestTime(),
                phone,
                email,
                request.getMessage(),
                request.isEmergency(),
                distance,
                hospital.getCity()
        );
    }

    public List<BloodRequestResponse> getHospitalRequests(String hospitalEmail) {
        User user = userRepository.findByEmail(hospitalEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        HospitalProfile hospital = hospitalRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Hospital profile not found"));
        return requestRepository.findByHospital(hospital).stream().map(this::mapToResponse).toList();
    }

    public BloodRequestResponse sendRequest(String hospitalEmail, Long donorId, boolean emergency) {
        if (!rateLimitingService.isAllowed("BLOOD_REQUEST_" + hospitalEmail)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Request limit exceeded. Please wait a minute before sending more requests.");
        }
        User user = userRepository.findByEmail(hospitalEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        HospitalProfile hospital = hospitalRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Hospital profile not found"));
        DonorProfile donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        if (requestRepository.existsByHospitalAndDonorAndStatusIn(hospital, donor, java.util.Arrays.asList("PENDING", "ACCEPTED"))) {
            logger.warn("action=create_request_failed reason=duplicate hospital=\"{}\" donor=\"{}\"", hospitalEmail, donor.getUser().getEmail());
            throw new RuntimeException("You already have a pending or accepted request for this donor.");
        }

        BloodRequest request = new BloodRequest(hospital, donor, "PENDING", null, LocalDateTime.now());
        request.setEmergency(emergency);
        BloodRequest saved = requestRepository.save(request);
        logger.info("action=create_request_success hospital=\"{}\" donor=\"{}\" emergency={}", hospitalEmail, donor.getUser().getEmail(), emergency);
        sendRequestNotification(saved);
        sendEmailToDonor(saved);
        return mapToResponse(saved);
    }

    public BloodRequestResponse sendRequestWithPayload(String authenticatedEmail, BloodRequestPayload payload) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        HospitalProfile hospital = hospitalRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Hospital profile not found"));
        DonorProfile donor = donorRepository.findById(payload.getDonorId())
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        if (requestRepository.existsByHospitalAndDonorAndStatusIn(hospital, donor, java.util.Arrays.asList("PENDING", "ACCEPTED"))) {
            logger.warn("action=create_request_failed reason=duplicate hospital=\"{}\" donor=\"{}\"", authenticatedEmail, donor.getUser().getEmail());
            throw new RuntimeException("You already have a pending or accepted request for this donor.");
        }

        BloodRequest request = new BloodRequest(hospital, donor, "PENDING", payload.getMessage(), LocalDateTime.now());
        request.setEmergency(false);
        BloodRequest saved = requestRepository.save(request);
        logger.info("action=create_request_success type=payload hospital=\"{}\" donor=\"{}\"", authenticatedEmail, donor.getUser().getEmail());
        sendRequestNotification(saved);
        sendEmailToDonor(saved);
        return mapToResponse(saved);
    }

    private void sendRequestNotification(BloodRequest request) {
        String message = (request.isEmergency() ? "🚨 EMERGENCY: " : "") +
                request.getHospital().getHospitalName() + " requested " + 
                request.getDonor().getBloodGroup() + " blood.";
        notificationService.createNotification(message, request.getDonor().getUser());
    }

    public List<BloodRequestResponse> getDonorRequests(String donorEmail) {
        User user = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DonorProfile donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Donor profile not found"));
        return requestRepository.findByDonor(donor).stream().map(this::mapToResponse).toList();
    }

    public BloodRequestResponse updateStatus(Long requestId, String status) {
        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            DonorProfile donor = request.getDonor();
            donor.setAvailable(false);
            donor.setLastDonationDate(java.time.LocalDate.now());
            donorRepository.save(donor);
        }
        BloodRequest saved = requestRepository.save(request);
        logger.info("action=update_request_status request_id={} status=\"{}\"", requestId, status);
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            sendEmailToHospital(saved);
        }
        return mapToResponse(saved);
    }

    public BloodRequestResponse markAsCompleted(String hospitalEmail, Long requestId) {
        User user = userRepository.findByEmail(hospitalEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        HospitalProfile hospital = hospitalRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Hospital profile not found"));
        BloodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Blood request not found"));

        if (!request.getHospital().getId().equals(hospital.getId())) {
            logger.warn("action=mark_complete_failed reason=not_owner hospital=\"{}\" request_id={}", hospitalEmail, requestId);
            throw new RuntimeException("You can only complete your own blood requests");
        }

        if (!"ACCEPTED".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("Only ACCEPTED requests can be marked as completed");
        }

        request.setStatus("COMPLETED");
        BloodRequest saved = requestRepository.save(request);
        logger.info("action=mark_complete_success hospital=\"{}\" request_id={}", hospitalEmail, requestId);
        return mapToResponse(saved);
    }


    private void sendEmailToDonor(BloodRequest request) {
        String donorName = request.getDonor().getUser().getName();
        String hospitalName = request.getHospital().getHospitalName();
        String bloodGroup = request.getDonor().getBloodGroup();
        String donorEmail = request.getDonor().getUser().getEmail();

        String subject = "Blood Request Received – Blood Connect";
        String body = String.format(
            "Dear %s, <br><br> " +
            "Hospital <b>%s</b> has sent you a blood request for blood group <b>%s</b>.<br> " +
            "Please log in to Blood Connect to accept or decline.<br><br> " +
            "Thank you for saving lives.",
            donorName, hospitalName, bloodGroup
        );

        emailService.sendEmail(donorEmail, subject, body);
    }

    private void sendEmailToHospital(BloodRequest request) {
        String hospitalName = request.getHospital().getHospitalName();
        String donorName = request.getDonor().getUser().getName();
        String bloodGroup = request.getDonor().getBloodGroup();
        String hospitalEmail = request.getHospital().getUser().getEmail();

        String subject = "Blood Request Accepted – Blood Connect";
        String body = String.format(
            "Dear %s, <br><br> " +
            "Donor <b>%s</b> has accepted your blood request for blood group <b>%s</b>.<br> " +
            "Please coordinate with the donor for further steps.<br><br> " +
            "Blood Connect Team",
            hospitalName, donorName, bloodGroup
        );

        emailService.sendEmail(hospitalEmail, subject, body);
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void expireOldRequests() {
        LocalDateTime fortyEightHoursAgo = LocalDateTime.now().minusHours(48);
        List<BloodRequest> oldRequests = requestRepository.findByStatusAndRequestTimeBefore("PENDING", fortyEightHoursAgo);
        
        if (!oldRequests.isEmpty()) {
            oldRequests.forEach(req -> req.setStatus("EXPIRED"));
            requestRepository.saveAll(oldRequests);
            logger.info("action=expire_requests count={}", oldRequests.size());
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public String broadcastEmergency(String hospitalEmail, String bloodGroup, Double lat, Double lon) {
        if (!rateLimitingService.isAllowed("EMERGENCY_BROADCAST_" + hospitalEmail)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Emergency broadcast limit exceeded. Please wait before broadcasting again.");
        }
        User hospitalUser = userRepository.findByEmail(hospitalEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        HospitalProfile hospital = hospitalRepository.findByUser(hospitalUser)
                .orElseThrow(() -> new RuntimeException("Hospital profile not found"));
        List<DonorProfile> donors = donorRepository.findAll();
        int sent = 0;
        for (DonorProfile donor : donors) {
            if (!donor.isAvailable() || !donor.getBloodGroup().equalsIgnoreCase(bloodGroup)) continue;
            if (donor.getLatitude() == null || donor.getLongitude() == null) continue;
            if (calculateDistance(lat, lon, donor.getLatitude(), donor.getLongitude()) <= 10) {
                BloodRequest request = new BloodRequest(hospital, donor, "PENDING", "EMERGENCY BROADCAST", LocalDateTime.now());
                request.setEmergency(true);
                requestRepository.save(request);
                notificationService.createNotification("🚨 EMERGENCY blood request from " + hospital.getHospitalName(), donor.getUser());
                sent++;
            }
        }
        return "Emergency request sent to " + sent + " donors";
    }
}
