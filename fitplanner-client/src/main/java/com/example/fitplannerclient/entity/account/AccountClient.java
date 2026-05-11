package com.example.fitplannerclient.entity.account;

import com.example.fitplannerclient.entity.profile.ProfileType;

public class AccountClient {
    private String email;
    private String password;
    private String refreshToken;
    private String accessToken;
    private final ProfileType profileType;

    public AccountClient( String email, String password, String refreshToken, String accessToken, ProfileType profileType) {
        this.email = email;
        this.password = password;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.profileType = profileType;
    }

    public String getEmail() {
        return email;
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
