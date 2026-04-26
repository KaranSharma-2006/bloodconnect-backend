package com.bloodconnect.bloodconnect.config;

import com.bloodconnect.bloodconnect.model.Role;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("karsn@11").isEmpty()) {
                User admin = new User(
                        "Admin",
                        "karsn@11",
                        passwordEncoder.encode("123456"),
                        Role.ROLE_ADMIN,
                        true,
                        false
                );
                userRepository.save(admin);
                System.out.println("Admin user seeded: karsn@11 / 123456");
            }
        };
    }
}
