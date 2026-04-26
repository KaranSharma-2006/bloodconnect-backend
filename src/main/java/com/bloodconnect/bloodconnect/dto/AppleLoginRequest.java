package com.bloodconnect.bloodconnect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppleLoginRequest {
    private String token; // Apple ID token
    
    @JsonProperty("user")
    private AppleUser user; // User info (only available on first sign-in)

    public AppleLoginRequest() {}

    public AppleLoginRequest(String token, AppleUser user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AppleUser getUser() {
        return user;
    }

    public void setUser(AppleUser user) {
        this.user = user;
    }

    public static class AppleUser {
        private String name;
        private String email;

        public AppleUser() {}

        public AppleUser(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
