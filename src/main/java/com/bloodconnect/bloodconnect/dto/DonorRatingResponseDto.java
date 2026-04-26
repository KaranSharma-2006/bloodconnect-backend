package com.bloodconnect.bloodconnect.dto;

public class DonorRatingResponseDto {

    private Double averageRating;
    private Long totalRatings;
    private String message;

    public DonorRatingResponseDto() {}

    public DonorRatingResponseDto(Double averageRating, Long totalRatings, String message) {
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
        this.message = message;
    }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
