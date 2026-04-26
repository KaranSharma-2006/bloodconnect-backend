package com.bloodconnect.bloodconnect.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bloodconnect.bloodconnect.dto.AdminComplaintResponseDto;
import com.bloodconnect.bloodconnect.dto.AdminReplyDto;
import com.bloodconnect.bloodconnect.dto.ComplaintResponseDto;
import com.bloodconnect.bloodconnect.dto.ComplaintSubmitDto;
import com.bloodconnect.bloodconnect.model.Complaint;
import com.bloodconnect.bloodconnect.model.ComplaintCategory;
import com.bloodconnect.bloodconnect.model.ComplaintStatus;
import com.bloodconnect.bloodconnect.repository.ComplaintRepository;

@Service
public class ComplaintService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);
    private final ComplaintRepository complaintRepository;
    private final EmailService emailService;

    public ComplaintService(ComplaintRepository complaintRepository, EmailService emailService) {
        this.complaintRepository = complaintRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String submitComplaint(String userEmail, String role, ComplaintSubmitDto dto) {
        logger.info("Submitting complaint for user: {} with role: {}", userEmail, role);

        ComplaintCategory category;
        try {
            category = ComplaintCategory.valueOf(dto.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid complaint category: {}", dto.getCategory());
            throw new RuntimeException("Invalid complaint category");
        }

        Complaint complaint = new Complaint(
                userEmail,
                role,
                dto.getTitle(),
                dto.getDescription(),
                category
        );

        complaint = complaintRepository.save(complaint);
        logger.info("Complaint saved with ID: {}", complaint.getId());

        try {
            sendSubmissionEmail(userEmail, category.name(), complaint.getId());
        } catch (Exception e) {
            logger.warn("Failed to send submission email for complaint ID: {}, error: {}", complaint.getId(), e.getMessage());
            // Don't throw exception - email failure shouldn't block complaint submission
        }

        return "Complaint submitted successfully. Your complaint ID is " + complaint.getId();
    }

    public List<ComplaintResponseDto> getMyComplaints(String userEmail) {
        logger.info("Fetching complaints for user: {}", userEmail);
        return complaintRepository.findBySubmittedBy(userEmail).stream()
                .map(c -> new ComplaintResponseDto(
                        c.getId(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getCategory().name(),
                        c.getStatus().name(),
                        c.getAdminReply(),
                        c.getCreatedAt(),
                        c.getResolvedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<AdminComplaintResponseDto> getAllComplaints() {
        logger.info("Admin fetching all complaints");
        return complaintRepository.findAll().stream()
                .map(c -> new AdminComplaintResponseDto(
                        c.getId(),
                        c.getSubmittedBy(),
                        c.getRole(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getCategory().name(),
                        c.getStatus().name(),
                        c.getAdminReply(),
                        c.getCreatedAt(),
                        c.getResolvedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String replyToComplaint(AdminReplyDto dto) {
        logger.info("Admin replying to complaint ID: {}", dto.getComplaintId());
        
        Complaint complaint = complaintRepository.findById(dto.getComplaintId())
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + dto.getComplaintId()));

        complaint.setAdminReply(dto.getReply());
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.save(complaint);

        logger.info("Reply saved and status updated to IN_PROGRESS for complaint ID: {}", complaint.getId());

        try {
            sendReplyEmail(complaint.getSubmittedBy(), complaint.getId(), dto.getReply());
        } catch (Exception e) {
            logger.warn("Failed to send reply email for complaint ID: {}, error: {}", complaint.getId(), e.getMessage());
            // Don't throw exception - email failure shouldn't block reply
        }

        return "Reply sent successfully";
    }

    @Transactional
    public String resolveComplaint(Long complaintId) {
        logger.info("Resolving complaint ID: {}", complaintId);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        logger.info("Complaint ID: {} marked as RESOLVED", complaintId);

        try {
            sendResolutionEmail(complaint.getSubmittedBy(), complaint.getId());
        } catch (Exception e) {
            logger.warn("Failed to send resolution email for complaint ID: {}, error: {}", complaint.getId(), e.getMessage());
            // Don't throw exception - email failure shouldn't block resolution
        }

        return "Complaint resolved successfully";
    }

    private void sendSubmissionEmail(String email, String category, Long complaintId) {
        String subject = "Complaint Received – Blood Connect Support";
        String body = String.format(
                "Dear User,<br><br>" +
                "We have received your complaint regarding <b>%s</b>.<br>" +
                "Our support team will get back to you shortly.<br><br>" +
                "Your Complaint ID: <b>%d</b><br>" +
                "Status: <b>OPEN</b><br><br>" +
                "Blood Connect Support Team",
                category, complaintId
        );
        emailService.sendEmail(email, subject, body);
    }

    private void sendReplyEmail(String email, Long complaintId, String adminReply) {
        String subject = "Response to Your Complaint – Blood Connect Support";
        String body = String.format(
                "Dear User,<br><br>" +
                "Our support team has replied to your complaint (ID: <b>%d</b>).<br><br>" +
                "<b>Admin Reply:</b><br>" +
                "%s<br><br>" +
                "If your issue is resolved, no further action is needed. Otherwise, you may submit a new complaint.<br><br>" +
                "Blood Connect Support Team",
                complaintId, adminReply
        );
        emailService.sendEmail(email, subject, body);
    }

    private void sendResolutionEmail(String email, Long complaintId) {
        String subject = "Complaint Resolved – Blood Connect Support";
        String body = String.format(
                "Dear User,<br><br>" +
                "Your complaint (ID: <b>%d</b>) has been marked as <b>RESOLVED</b>.<br><br>" +
                "Thank you for reaching out to Blood Connect Support.<br>" +
                "We hope your issue has been addressed.<br><br>" +
                "Blood Connect Support Team",
                complaintId
        );
        emailService.sendEmail(email, subject, body);
    }
}
