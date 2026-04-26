package com.bloodconnect.bloodconnect.dto;

public class NearbyDonorResponse {

    private Long id;
    private String name;
    private String bloodGroup;
    private String city;
    private double distanceKm;

    public NearbyDonorResponse(Long id, String name,
                               String bloodGroup, String city,
                               double distanceKm) {
        this.id = id;
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.distanceKm = distanceKm;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getBloodGroup() { return bloodGroup; }

    public String getCity() { return city; }

    public double getDistanceKm() { return distanceKm; }
}