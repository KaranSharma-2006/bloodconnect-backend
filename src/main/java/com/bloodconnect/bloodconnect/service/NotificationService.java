package com.bloodconnect.bloodconnect.service;

import com.bloodconnect.bloodconnect.model.Notification;
import com.bloodconnect.bloodconnect.model.User;
import com.bloodconnect.bloodconnect.repository.NotificationRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(String message, User user) {

        Notification notification = new Notification(message, user);

        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {

        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }


}