package com.bloodconnect.bloodconnect.dto;

public class RatingRequestDto {

    private Long bloodRequestId;
    private Integer rating;
    private String feedback;

    public RatingRequestDto() {}

    public Long getBloodRequestId() { return bloodRequestId; }
    public void setBloodRequestId(Long bloodRequestId) { this.bloodRequestId = bloodRequestId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
