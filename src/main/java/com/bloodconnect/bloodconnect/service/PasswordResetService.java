package com.bloodconnect.bloodconnect.service;

import com.bloodconnect.bloodconnect.model.PasswordResetToken;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.PasswordResetTokenRepository;
import com.bloodconnect.bloodconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final RateLimitingService rateLimitingService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                RateLimitingService rateLimitingService,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.rateLimitingService = rateLimitingService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        // Enforce rate limiting: 3 requests per hour max
        String rateLimitKey = "FORGOT_PW_" + email.toLowerCase();
        if (!rateLimitingService.isAllowed(rateLimitKey)) {
            // Throwing Too Many Requests could reveal information, but usually it's acceptable for abuse prevention.
            logger.warn("Rate limit exceeded for password reset request on email: {}", email);
            return; // We silently return or wait, but user requirement says returning generic message. We'll let RateLimitingService just return false, and we silently return.
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.info("Password reset requested for non-existent email: {}", email);
            return; // Prevent email enumeration
        }

        User user = userOpt.get();

        // Generate 32-byte raw token
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Hash token for database
        String tokenHash = hashToken(rawToken);

        // Create token entity
        PasswordResetToken tokenEntity = new PasswordResetToken(
                user.getId(),
                tokenHash,
                LocalDateTime.now().plusMinutes(30)
        );
        tokenRepository.save(tokenEntity);

        // Send Email
        String resetLink = "http://localhost:5173/reset-password?token=" + rawToken;
        String subject = "Reset your password - Blood Connect";
        String body = "Hello " + user.getName() + ",\n\n" +
                "You requested to reset your password. Click the link below to set a new password:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 30 minutes.\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best,\nBlood Connect Team";

        emailService.sendEmail(email, subject, body);
        logger.info("Sent password reset link for user ID: {}", user.getId());
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);

        PasswordResetToken tokenEntity = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token."));

        if (tokenEntity.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This token has already been used.");
        }

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This token has expired.");
        }

        User user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate token
        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);

        logger.info("Password successfully reset for user ID: {}", user.getId());
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
