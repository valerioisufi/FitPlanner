package com.example.fitplannerclient.entity.account;

public class AccountClient {
    private final ProfileType profileType;
    private String username;
    private String password;
    private String refreshToken;
    private String accessToken;

    public AccountClient(ProfileType profileType, String username, String password, String refreshToken, String accessToken) {
        this.profileType = profileType;
        this.username = username;
        this.password = password;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public ProfileType getProfileType() {
        return profileType;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public String getAccessToken() {
        return accessToken;
    }
}
