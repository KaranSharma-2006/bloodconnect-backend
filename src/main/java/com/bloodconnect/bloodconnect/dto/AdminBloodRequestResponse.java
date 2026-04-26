package com.bloodconnect.bloodconnect.dto;

import java.time.LocalDateTime;

public class AdminBloodRequestResponse {

    private Long id;
    private Long hospitalId;
    private Long donorId;
    private String status;
    private LocalDateTime requestTime;
    private boolean emergency;

    public AdminBloodRequestResponse(Long id, Long hospitalId, Long donorId, String status, LocalDateTime requestTime, boolean emergency) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.donorId = donorId;
        this.status = status;
        this.requestTime = requestTime;
        this.emergency = emergency;
    }

    public Long getId() { return id; }
    public Long getHospitalId() { return hospitalId; }
    public Long getDonorId() { return donorId; }
    public String getStatus() { return status; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public boolean isEmergency() { return emergency; }
}
