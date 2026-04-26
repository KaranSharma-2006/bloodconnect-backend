package com.bloodconnect.bloodconnect.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "hospital_profiles",
        indexes = {
                @Index(name = "idx_hosp_location", columnList = "latitude, longitude"),
                @Index(name = "idx_hosp_city", columnList = "city")
        }
)
public class HospitalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hospitalName;

    private String licenseNumber;

    private String city;
    private String state;
    private String area;
    private String address;
    private String pincode;

    private Double latitude;
    private Double longitude;

    private String contactNumber;
    private String requiredBloodTypes;

    private boolean verified;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public HospitalProfile() {}

    public HospitalProfile(String hospitalName,
                           String licenseNumber,
                           String city, String state, String area,
                           String address, String pincode,
                           Double latitude, Double longitude,
                           String contactNumber,
                           String requiredBloodTypes,
                           boolean verified,
                           User user) {
        this.hospitalName = hospitalName;
        this.licenseNumber = licenseNumber;
        this.city = city;
        this.state = state;
        this.area = area;
        this.address = address;
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNumber = contactNumber;
        this.requiredBloodTypes = requiredBloodTypes;
        this.verified = verified;
        this.user = user;
    }

    public Long getId() { return id; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

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

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getRequiredBloodTypes() { return requiredBloodTypes; }
    public void setRequiredBloodTypes(String requiredBloodTypes) { this.requiredBloodTypes = requiredBloodTypes; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}