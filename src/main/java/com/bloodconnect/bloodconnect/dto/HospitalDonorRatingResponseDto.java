package com.bloodconnect.bloodconnect.dto;

public class HospitalDonorRatingResponseDto {

    private Long donorId;
    private String donorName;
    private String bloodGroup;
    private Double averageRating;
    private Long totalDonations;

    public HospitalDonorRatingResponseDto() {}

    public HospitalDonorRatingResponseDto(Long donorId, String donorName, String bloodGroup,
                                          Double averageRating, Long totalDonations) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.bloodGroup = bloodGroup;
        this.averageRating = averageRating;
        this.totalDonations = totalDonations;
    }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getTotalDonations() { return totalDonations; }
    public void setTotalDonations(Long totalDonations) { this.totalDonations = totalDonations; }
}
