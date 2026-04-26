package com.bloodconnect.bloodconnect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.bloodconnect.bloodconnect.dto.DonorSearchResponse;
import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.model.HospitalProfile;
import com.bloodconnect.bloodconnect.repository.BloodRequestRepository;
import com.bloodconnect.bloodconnect.repository.DonorProfileRepository;
import com.bloodconnect.bloodconnect.repository.HospitalProfileRepository;

@Service
public class DonorService {

    private static final Logger logger = LoggerFactory.getLogger(DonorService.class);

    private final DonorProfileRepository donorRepository;
    private final HospitalProfileRepository hospitalRepository;
    private final BloodRequestRepository bloodRequestRepository;

    public DonorService(DonorProfileRepository donorRepository,
                        HospitalProfileRepository hospitalRepository,
                        BloodRequestRepository bloodRequestRepository) {
        this.donorRepository = donorRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodRequestRepository = bloodRequestRepository;
    }

    public List<DonorSearchResponse> searchDonors(String bloodGroup, Double radius, String hospitalEmail, boolean includeUnverified) {
        
        bloodGroup = bloodGroup.replace(" ", "+").trim();
        logger.info("action=search_donors bloodGroup=\"{}\" radius={} hospitalEmail=\"{}\" includeUnverified={}", 
                bloodGroup, radius, hospitalEmail, includeUnverified);

        HospitalProfile hospital = hospitalRepository.findByUserEmail(hospitalEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital profile not found"));

        List<DonorProfile> donors;
        boolean isFallback = false;

        if (hospital.getLatitude() != null && hospital.getLongitude() != null && radius != null && radius > 0) {
            double lat = hospital.getLatitude();
            double lng = hospital.getLongitude();
            
            // Bounding box calculation
            double latOffset = radius / 111.0;
            double lngOffset = radius / (111.0 * Math.cos(Math.toRadians(lat)));

            donors = donorRepository.findNearbyDonors(
                    bloodGroup, lat, lng, radius,
                    lat - latOffset, lat + latOffset,
                    lng - lngOffset, lng + lngOffset,
                    includeUnverified
            );
        } else {
            logger.info("action=fallback_city_search hospitalEmail=\"{}\" city=\"{}\"", hospitalEmail, hospital.getCity());
            donors = donorRepository.findByBloodGroupIgnoreCaseAndCityIgnoreCaseAndAvailableTrue(bloodGroup, hospital.getCity());
            if (!includeUnverified) {
                donors = donors.stream().filter(d -> d.getUser().isVerified()).collect(Collectors.toList());
            }
            isFallback = true;
        }

        // Fetch request statuses for this hospital and these donors to avoid N+1 queries
        List<com.bloodconnect.bloodconnect.model.BloodRequest> requests = bloodRequestRepository.findByHospital(hospital);
        java.util.Map<Long, String> statusMap = requests.stream()
                .filter(req -> req.getDonor() != null && req.getStatus() != null)
                .collect(Collectors.toMap(
                        req -> req.getDonor().getId(),
                        req -> req.getStatus(),
                        (s1, s2) -> s1 
                ));

        List<DonorSearchResponse> results = donors.stream().map(donor -> {
            Double distance = null;
            if (hospital.getLatitude() != null && donor.getLatitude() != null) {
                distance = calculateDistance(hospital.getLatitude(), hospital.getLongitude(),
                        donor.getLatitude(), donor.getLongitude());
                if (!Double.isNaN(distance)) {
                    distance = Math.round(distance * 10.0) / 10.0;
                } else {
                    distance = null; // Mapping NaN to null for JSON safety
                }
            }

            String status = statusMap.getOrDefault(donor.getId(), null);
            boolean isVerified = donor.getUser() != null && donor.getUser().isVerified();
            
            DonorSearchResponse response = new DonorSearchResponse(
                    donor.getId(),
                    donor.getUser() != null ? donor.getUser().getName() : "Unknown",
                    donor.getBloodGroup(),
                    donor.getCity(),
                    donor.isAvailable(),
                    distance,
                    status,
                    isVerified
            );

            // Information Disclosure control: only share contact for ACCEPTED requests
            if ("ACCEPTED".equals(status)) {
                response.setPhone(donor.getPhone());
                response.setEmail(donor.getUser().getEmail());
            }

            return response;
        })
        .sorted((a, b) -> {
            // Verified first
            if (a.isVerified() != b.isVerified()) {
                return a.isVerified() ? -1 : 1;
            }
            // Then by distance
            return java.util.Comparator.comparing(DonorSearchResponse::getDistance, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                    .compare(a, b);
        })
        .collect(Collectors.toList());

        logger.info("action=search_donors_complete count={} isFallback={}", results.size(), isFallback);
        return results;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        // Correct for precision errors that might push 'a' out of [0, 1] range
        a = Math.max(0, Math.min(1, a));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}