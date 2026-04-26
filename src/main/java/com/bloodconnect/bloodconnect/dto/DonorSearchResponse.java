package com.bloodconnect.bloodconnect.dto;

public class DonorSearchResponse {

    private Long id;
    private String name;
    private String bloodGroup;
    private String city;
    private boolean available;
    private Double distance;
    private String requestStatus;
    private boolean verified;
    private String phone;
    private String email;

    public DonorSearchResponse(Long id, String name, String bloodGroup, String city, boolean available, Double distance, String requestStatus, boolean verified) {
        this.id = id;
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.available = available;
        this.distance = distance;
        this.requestStatus = requestStatus;
        this.verified = verified;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public boolean isAvailable() {
        return available;
    }

    public Double getDistance() {
        return distance;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isVerified() {
        return verified;
    }
}