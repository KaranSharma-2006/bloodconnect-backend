package com.bloodconnect.bloodconnect.repository;

import com.bloodconnect.bloodconnect.model.BloodRequest;
import com.bloodconnect.bloodconnect.model.DonorRating;
import com.bloodconnect.bloodconnect.model.HospitalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonorRatingRepository extends JpaRepository<DonorRating, Long> {

    boolean existsByBloodRequestAndHospital(BloodRequest bloodRequest, HospitalProfile hospital);

    List<DonorRating> findByDonorId(Long donorId);

    @Query("SELECT AVG(r.rating) FROM DonorRating r WHERE r.donor.id = :donorId")
    Double findAverageRatingByDonorId(@Param("donorId") Long donorId);

    Long countByDonorId(Long donorId);
}
