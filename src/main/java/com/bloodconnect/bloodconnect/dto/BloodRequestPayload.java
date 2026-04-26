package com.bloodconnect.bloodconnect.dto;

public class BloodRequestPayload {
    private Long hospitalId;
    private Long donorId;
    private String bloodGroup;
    private String message;

    // Getters and Setters
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
