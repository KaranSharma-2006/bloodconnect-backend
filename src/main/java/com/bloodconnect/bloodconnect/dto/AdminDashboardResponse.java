package com.bloodconnect.bloodconnect.dto;

public class AdminDashboardResponse {

    private long totalDonors;
    private long totalHospitals;
    private long activeUsers;
    private long pendingVerification;
    private long totalBloodRequests;
    private long pendingBloodRequests;

    public AdminDashboardResponse(long totalDonors,
                                  long totalHospitals,
                                  long activeUsers,
                                  long pendingVerification,
                                  long totalBloodRequests,
                                  long pendingBloodRequests) {

        this.totalDonors = totalDonors;
        this.totalHospitals = totalHospitals;
        this.activeUsers = activeUsers;
        this.pendingVerification = pendingVerification;
        this.totalBloodRequests = totalBloodRequests;
        this.pendingBloodRequests = pendingBloodRequests;
    }

    public long getTotalDonors() { return totalDonors; }
    public long getTotalHospitals() { return totalHospitals; }
    public long getActiveUsers() { return activeUsers; }
    public long getPendingVerification() { return pendingVerification; }
    public long getTotalBloodRequests() { return totalBloodRequests; }
    public long getPendingBloodRequests() { return pendingBloodRequests; }
}