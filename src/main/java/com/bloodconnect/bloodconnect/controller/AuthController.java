package com.bloodconnect.bloodconnect.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bloodconnect.bloodconnect.LoginDebugHelper;
import com.bloodconnect.bloodconnect.dto.AppleLoginRequest;
import com.bloodconnect.bloodconnect.dto.ForgotPasswordRequest;
import com.bloodconnect.bloodconnect.dto.GoogleLoginRequest;
import com.bloodconnect.bloodconnect.dto.LoginRequest;
import com.bloodconnect.bloodconnect.dto.RegisterRequest;
import com.bloodconnect.bloodconnect.dto.ResetPasswordRequest;
import com.bloodconnect.bloodconnect.model.Role;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.UserRepository;
import com.bloodconnect.bloodconnect.security.JwtUtil;
import com.bloodconnect.bloodconnect.service.OAuthService;
import com.bloodconnect.bloodconnect.service.UserService;
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OAuthService oAuthService;
    private final com.bloodconnect.bloodconnect.repository.BloodRequestRepository requestRepository;
    private final com.bloodconnect.bloodconnect.service.PasswordResetService passwordResetService;
    private final LoginDebugHelper loginDebugHelper;

    // Constructor Injection
    public AuthController(UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          OAuthService oAuthService,
                          com.bloodconnect.bloodconnect.repository.BloodRequestRepository requestRepository,
                          com.bloodconnect.bloodconnect.service.PasswordResetService passwordResetService,
                          LoginDebugHelper loginDebugHelper) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.oAuthService = oAuthService;
        this.requestRepository = requestRepository;
        this.passwordResetService = passwordResetService;
        this.loginDebugHelper = loginDebugHelper;
    }

    @GetMapping("/stats")
    public Map<String, Long> getPublicStats() {
        long unitsSecured = requestRepository.count(); // Total blood requests
        long livesImpacted = userRepository.countByRole(Role.ROLE_DONOR) * 3; // 1 donor saves 3 lives
        
        return Map.of(
            "unitsSecured", unitsSecured,
            "livesImpacted", livesImpacted
        );
    }

    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String register(
            @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "documentFile", required = false) MultipartFile documentFile) {
        return userService.registerWithVerification(request, documentFile);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt for email: {}", request.getEmail());
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            logger.info("User found: {}, role: {}", user.getEmail(), user.getRole());

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.warn("Invalid password for user: {}", request.getEmail());
                throw new RuntimeException("Invalid password");
            }

            if (user.isBlocked()) {
                logger.warn("Login attempt from blocked user: {}", request.getEmail());
                throw new RuntimeException("Your account has been blocked by the administrator");
            }

            logger.info("Generating JWT token for user: {}", user.getEmail());
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getName(), user.isVerified());
            logger.info("JWT token generated successfully for user: {}", user.getEmail());
            return token;
        } catch (RuntimeException e) {
            logger.error("RuntimeException in login: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception in login: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    @GetMapping("/debug/{email}")
    public String debugLogin(@PathVariable String email) {
        loginDebugHelper.debugLogin(email, "123456");
        return "Check backend console for debug output";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        // Always return the same response to prevent email enumeration
        return "If an account with that email exists, we have sent a reset link to it.";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return "Password successfully reset.";
    }

    @GetMapping("/encode/{password}")
    public String encode(@PathVariable String password) {
        return passwordEncoder.encode(password);
    }

    @PostMapping("/google")
    public String googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            // Verify Google ID token
            Map<String, String> claims = oAuthService.verifyGoogleToken(request.getToken());

            String email = claims.get("email");
            String name = claims.get("name");

            // Check if user exists, if not create them
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update name if not set
                if ((user.getName() == null || user.getName().isEmpty()) && name != null) {
                    user.setName(name);
                    userRepository.save(user);
                }
            } else {
                // Create new user with default role as DONOR
                user = new User();
                user.setEmail(email);
                user.setName(name != null ? name : "Google User");
                user.setRole(Role.ROLE_DONOR);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password for OAuth users
                user.setVerified(false); // Will need verification
                user = userRepository.save(user);
            }

            return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getName(), user.isVerified());
        } catch (Exception e) {
            throw new RuntimeException("Google login failed: " + e.getMessage());
        }
    }

    @PostMapping("/apple")
    public String appleLogin(@RequestBody AppleLoginRequest request) {
        try {
            // Verify Apple ID token
            Map<String, String> claims = oAuthService.verifyAppleToken(request.getToken());

            String email = claims.get("email");
            
            // Use user info from request if email not in token
            String name = null;
            if (request.getUser() != null) {
                email = request.getUser().getEmail() != null ? request.getUser().getEmail() : email;
                name = request.getUser().getName();
            }

            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email not provided by Apple");
            }

            // Check if user exists, if not create them
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update name if not set
                if ((user.getName() == null || user.getName().isEmpty()) && name != null) {
                    user.setName(name);
                    userRepository.save(user);
                }
            } else {
                // Create new user with default role as DONOR
                user = new User();
                user.setEmail(email);
                user.setName(name != null ? name : "Apple User");
                user.setRole(Role.ROLE_DONOR);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password for OAuth users
                user.setVerified(false); // Will need verification
                user = userRepository.save(user);
            }

            return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getName(), user.isVerified());
        } catch (Exception e) {
            throw new RuntimeException("Apple login failed: " + e.getMessage());
        }
    }
}