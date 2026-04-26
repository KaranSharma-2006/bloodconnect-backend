package com.bloodconnect.bloodconnect.repository;

import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {

    Optional<DonorProfile> findByUser(User user);

    List<DonorProfile> findByBloodGroupIgnoreCaseAndCityIgnoreCaseAndAvailableTrue(
            String bloodGroup,
            String city
    );

    @Query(value = """
SELECT d.*
FROM donor_profiles d
JOIN users u ON d.user_id = u.id
WHERE d.blood_group = :bloodGroup
AND d.available = true
AND (u.verified = true OR :includeUnverified = true)
AND d.latitude BETWEEN :minLat AND :maxLat
AND d.longitude BETWEEN :minLng AND :maxLng
HAVING (6371 * acos(
cos(radians(:lat)) * cos(radians(d.latitude)) *
cos(radians(d.longitude) - radians(:lng)) +
sin(radians(:lat)) * sin(radians(d.latitude))
)) < :radius
ORDER BY u.verified DESC, (6371 * acos(
cos(radians(:lat)) * cos(radians(d.latitude)) *
cos(radians(d.longitude) - radians(:lng)) +
sin(radians(:lat)) * sin(radians(d.latitude))
)) ASC
""", nativeQuery = true)
    List<DonorProfile> findNearbyDonors(
            @Param("bloodGroup") String bloodGroup,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            @Param("includeUnverified") boolean includeUnverified
    );
}