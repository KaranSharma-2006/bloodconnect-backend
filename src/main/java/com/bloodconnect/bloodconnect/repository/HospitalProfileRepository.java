package com.bloodconnect.bloodconnect.repository;

import com.bloodconnect.bloodconnect.model.HospitalProfile;
import com.bloodconnect.bloodconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalProfileRepository extends JpaRepository<HospitalProfile, Long> {

    Optional<HospitalProfile> findByUser(User user);

    Optional<HospitalProfile> findByUserEmail(String email);
}