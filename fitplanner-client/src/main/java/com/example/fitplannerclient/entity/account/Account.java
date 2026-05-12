package com.example.fitplannerclient.entity.account;

import com.example.fitplannercommon.ProfileBean;

public class Account {
    private final String email;
    private final String password;
    private final String refreshToken;
    private final String accessToken;
    private final ProfileBean.ProfileType profileType;

    public Account(String email, String password, String refreshToken, String accessToken, ProfileBean.ProfileType profileType) {
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
