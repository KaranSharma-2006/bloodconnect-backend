package com.bloodconnect.bloodconnect.dto;

import com.bloodconnect.bloodconnect.model.Role;
import java.time.LocalDate;

public class AdminUserDetailResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean verified;
    private boolean blocked;

    // Donor specific fields
    private String bloodGroup;
    private String phone;
    private String city;
    private boolean available;
    private LocalDate lastDonationDate;

    // Hospital specific fields
    private String hospitalName;
    private String licenseNumber;
    private String contactNumber;
    private String requiredBloodTypes;

    public AdminUserDetailResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getRequiredBloodTypes() { return requiredBloodTypes; }
    public void setRequiredBloodTypes(String requiredBloodTypes) { this.requiredBloodTypes = requiredBloodTypes; }
}
