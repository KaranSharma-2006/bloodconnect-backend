package com.bloodconnect.bloodconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private HospitalProfile hospital;

    @ManyToOne
    @JoinColumn(name = "donor_id")
    private DonorProfile donor;

    private boolean emergency;

    private String status; // PENDING, ACCEPTED, REJECTED
    private String message;
    private LocalDateTime requestTime;

    public BloodRequest() {}

    public BloodRequest(HospitalProfile hospital,
                        DonorProfile donor,
                        String status,
                        String message,
                        LocalDateTime requestTime) {
        this.hospital = hospital;
        this.donor = donor;
        this.status = status;
        this.message = message;
        this.requestTime = requestTime;
    }

    public Long getId() { return id; }

    public HospitalProfile getHospital() { return hospital; }
    public void setHospital(HospitalProfile hospital) { this.hospital = hospital; }

    public DonorProfile getDonor() { return donor; }
    public void setDonor(DonorProfile donor) { this.donor = donor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}