package com.bloodconnect.bloodconnect.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "donor_profiles",
        indexes = {
                @Index(name = "idx_blood_group", columnList = "bloodGroup"),
                @Index(name = "idx_available", columnList = "available"),
                @Index(name = "idx_location", columnList = "latitude, longitude"),
                @Index(name = "idx_city", columnList = "city")
        }
)

public class DonorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bloodGroup;
    private String phone;
    private String city;
    private String state;
    private String area;
    private String address;
    private String pincode;

    private boolean available;
    private LocalDate lastDonationDate;

    private Double latitude;
    private Double longitude;

    private boolean isVerified = false;


    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public DonorProfile() {}

    public DonorProfile(String bloodGroup, String phone,
                        String city, String state, String area,
                        String address, String pincode,
                        boolean available,
                        LocalDate lastDonationDate, User user) {
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.city = city;
        this.state = state;
        this.area = area;
        this.address = address;
        this.pincode = pincode;
        this.available = available;
        this.lastDonationDate = lastDonationDate;
        this.user = user;
    }

    public Long getId() { return id; }

    public String getBloodGroup() { return bloodGroup; }

    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) { this.available = available; }

    public LocalDate getLastDonationDate() { return lastDonationDate; }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}