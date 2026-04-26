package com.bloodconnect.bloodconnect.controller;

import com.bloodconnect.bloodconnect.dto.BloodRequestResponse;
import com.bloodconnect.bloodconnect.dto.BloodRequestPayload;
import com.bloodconnect.bloodconnect.dto.EmergencyRequest;
import com.bloodconnect.bloodconnect.service.BloodRequestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class BloodRequestController {

    private final BloodRequestService requestService;

    public BloodRequestController(BloodRequestService requestService) {
        this.requestService = requestService;
    }

    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @PostMapping("/send/{donorId}")
    public BloodRequestResponse sendRequest(
            @PathVariable Long donorId,
            @RequestParam(defaultValue = "false") boolean emergency) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return requestService.sendRequest(email, donorId, emergency);
    }

    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @PostMapping("")
    public BloodRequestResponse sendRequestWithPayload(@RequestBody BloodRequestPayload payload) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return requestService.sendRequestWithPayload(email, payload);
    }

    @PreAuthorize("hasRole('DONOR')")
    @GetMapping("/donor")
    public List<BloodRequestResponse> getDonorRequests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return requestService.getDonorRequests(email);
    }

    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    @PutMapping("/update/{id}")
    public BloodRequestResponse updateStatus(@PathVariable Long id, @RequestParam String status) {
        return requestService.updateStatus(id, status);
    }

    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @GetMapping("/hospital")
    public List<BloodRequestResponse> getHospitalRequests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return requestService.getHospitalRequests(email);
    }

    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @PostMapping("/broadcast")
    public String broadcastEmergency(@RequestBody EmergencyRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return requestService.broadcastEmergency(
                email,
                request.getBloodGroup(),
                request.getLatitude(),
                request.getLongitude()
        );
    }

    @PreAuthorize("hasAuthority('ROLE_HOSPITAL')")
    @PutMapping("/complete/{id}")
    public BloodRequestResponse markAsCompleted(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return requestService.markAsCompleted(email, id);
    }
}
