package com.bloodconnect.bloodconnect.repository;

import com.bloodconnect.bloodconnect.model.DonorVerification;
import com.bloodconnect.bloodconnect.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorVerificationRepository extends JpaRepository<DonorVerification, Long> {
    List<DonorVerification> findByDonorId(Long donorId);
    List<DonorVerification> findByStatus(VerificationStatus status);
    boolean existsByDonorIdAndStatusIn(Long donorId, List<VerificationStatus> statuses);
    Optional<DonorVerification> findByDonorIdAndStatus(Long donorId, VerificationStatus status);
    
    // Custom method to get the latest verification for a donor
    Optional<DonorVerification> findFirstByDonorIdOrderBySubmittedAtDesc(Long donorId);
}
