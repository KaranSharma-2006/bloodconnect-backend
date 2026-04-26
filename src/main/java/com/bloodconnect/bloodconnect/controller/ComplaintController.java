package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.dto.*;
import com.bloodconnect.bloodconnect.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_HOSPITAL')")
    public String submitComplaint(@Valid @RequestBody ComplaintSubmitDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        
        return complaintService.submitComplaint(email, role, dto);
    }

    @GetMapping("/my-complaints")
    @PreAuthorize("hasAnyAuthority('ROLE_DONOR', 'ROLE_HOSPITAL')")
    public List<ComplaintResponseDto> getMyComplaints() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return complaintService.getMyComplaints(email);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<AdminComplaintResponseDto> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    @PutMapping("/reply")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String replyToComplaint(@Valid @RequestBody AdminReplyDto dto) {
        return complaintService.replyToComplaint(dto);
    }

    @PutMapping("/resolve/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String resolveComplaint(@PathVariable Long id) {
        return complaintService.resolveComplaint(id);
    }
}
