package com.bloodconnect.bloodconnect.dto;

import java.time.LocalDateTime;

public class BloodRequestResponse {

    private Long id;
    private Long donorId;
    private String hospitalName;
    private String donorName;
    private String bloodGroup;
    private String status;
    private String message;
    private LocalDateTime requestTime;
    private String phone;
    private String email;
    private boolean emergency;

    private Double distance;
    private String hospitalCity;

    public BloodRequestResponse(Long id,
                                Long donorId,
                                String hospitalName,
                                String donorName,
                                String bloodGroup,
                                String status,
                                LocalDateTime requestTime,
                                String phone,
                                String email,
                                String message,
                                boolean emergency,
                                Double distance,
                                String hospitalCity) {

        this.id = id;
        this.donorId = donorId;
        this.hospitalName = hospitalName;
        this.donorName = donorName;
        this.bloodGroup = bloodGroup;
        this.status = status;
        this.requestTime = requestTime;
        this.phone = phone;
        this.email = email;
        this.message = message;
        this.emergency = emergency;
        this.distance = distance;
        this.hospitalCity = hospitalCity;
    }

    public Double getDistance() { return distance; }
    public String getHospitalCity() { return hospitalCity; }

    public String getMessage() { return message; }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public Long getId() { return id; }
    public Long getDonorId() { return donorId; }
    public String getHospitalName() { return hospitalName; }
    public String getDonorName() { return donorName; }
    public String getBloodGroup() { return bloodGroup; }
    public String getStatus() { return status; }
    public LocalDateTime getRequestTime() { return requestTime; }
}