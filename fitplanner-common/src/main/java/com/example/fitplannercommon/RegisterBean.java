package com.example.fitplannercommon;

public class RegisterBean {
    private String email;
    private String password;

    private ProfileBean profile;

    public RegisterBean() {}

    public RegisterBean(String email, String password, ProfileBean profile) {
        this.email = email;
        this.password = password;

        this.profile = profile;
    }

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