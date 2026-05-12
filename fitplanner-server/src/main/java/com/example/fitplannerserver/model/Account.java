package com.example.fitplannerserver.model;

public class Account {
    private final String email;
    private final String passwordHash;
    private String refreshToken;
    private final Role profileType;

    public Account(String email, String passwordHash, String refreshToken, Role profileType) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.refreshToken = refreshToken;
        this.profileType = profileType;
    }

    public String getUsername() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Role getProfileType() {
        return profileType;
    }

    public enum Role {
        TRAINER,
        ATHLETE
    }

}
