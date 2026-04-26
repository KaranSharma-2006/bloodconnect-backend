package com.bloodconnect.bloodconnect.repository;

import com.bloodconnect.bloodconnect.model.BloodRequest;
import com.bloodconnect.bloodconnect.model.DonorProfile;
import com.bloodconnect.bloodconnect.model.HospitalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByDonor(DonorProfile donor);

    List<BloodRequest> findByHospital(HospitalProfile hospital);

    boolean existsByHospitalAndDonorAndStatusIn(
            HospitalProfile hospital,
            DonorProfile donor,
            java.util.Collection<String> statuses
    );

    long countByStatus(String status);

    long countByDonorIdAndStatus(Long donorId, String status);

    List<BloodRequest> findByStatusAndRequestTimeBefore(String status, java.time.LocalDateTime time);
}