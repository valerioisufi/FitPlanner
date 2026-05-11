package com.example.fitplannerclient.dto.profile;

import com.example.fitplannerclient.entity.profile.ProfileType;

public class ProfileBean {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private final ProfileType profileType;
    private String username;


    public ProfileBean(String firstName, String lastName, String phoneNumber, String email, ProfileType profileType, String username, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.profileType = profileType;
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
