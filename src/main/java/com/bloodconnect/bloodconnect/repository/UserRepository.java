package com.bloodconnect.bloodconnect.repository;

import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    long countByRole(Role role);

    long countByVerifiedTrue();

    long countByVerifiedFalseAndRoleNot(Role role);
}