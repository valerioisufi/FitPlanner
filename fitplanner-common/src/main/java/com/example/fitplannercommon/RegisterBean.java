package com.example.fitplannercommon;

public class RegisterBean {
    private String username;
    private String password;

    private ProfileType profileType;

    private ProfileBean profile;

    public RegisterBean() {}

    public RegisterBean(String username, String password, ProfileType profileType, ProfileBean profile) {
        this.username = username;
        this.password = password;

        this.profileType = profileType;

        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ProfileType getProfileType() {
        return profileType;
    }
    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public ProfileBean getProfile() {
        return profile;
    }
    public void setProfile(ProfileBean profile) {
        this.profile = profile;
    }

    public enum ProfileType {
        TRAINER,
        ATHLETE
    }
}