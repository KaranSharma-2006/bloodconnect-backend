package com.bloodconnect.bloodconnect.dto;

import com.bloodconnect.bloodconnect.model.Role;

public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean verified;
    private boolean blocked;
    private String city;
    private String verificationStatus;

    public AdminUserResponse(Long id,
                             String name,
                             String email,
                             Role role,
                             boolean verified,
                             boolean blocked,
                             String city,
                             String verificationStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.verified = verified;
        this.blocked = blocked;
        this.city = city;
        this.verificationStatus = verificationStatus;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isVerified() { return verified; }
    public boolean isBlocked() { return blocked; }
    public String getCity() { return city; }
    public String getVerificationStatus() { return verificationStatus; }
}