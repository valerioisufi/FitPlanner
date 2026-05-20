package com.example.fitplannerclient.bean.auth;

import com.example.fitplannerclient.bean.ProfileBean;

public class RegisterBean {
    private String email;
    private String password;

    private ProfileBean profile;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public ProfileBean getProfile() {
        return profile;
    }

    public void setProfile(ProfileBean profile) {
        this.profile = profile;
    }
}