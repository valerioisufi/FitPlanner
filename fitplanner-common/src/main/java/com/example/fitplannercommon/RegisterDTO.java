package com.example.fitplannercommon;

public class RegisterDTO {
    private String email;
    private String password;

    private ProfileDTO profile;

    public RegisterDTO() {}

    public RegisterDTO(String email, String password, ProfileDTO profile) {
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

    public ProfileDTO getProfile() {
        return profile;
    }
    public void setProfile(ProfileDTO profile) {
        this.profile = profile;
    }

}