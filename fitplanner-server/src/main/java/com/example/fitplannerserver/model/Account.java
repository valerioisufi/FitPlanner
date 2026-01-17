package com.example.fitplannerserver.model;

public class Account {
    private final String username;
    private final String passwordHash;
    private String refreshToken;

    public Account(String username, String passwordHash, String refreshToken) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
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

}
