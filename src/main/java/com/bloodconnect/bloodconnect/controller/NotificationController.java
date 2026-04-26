package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.model.Notification;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.UserRepository;
import com.bloodconnect.bloodconnect.service.NotificationService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    @GetMapping
    public List<Notification> getNotifications() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationService.getUserNotifications(user);
    }
}