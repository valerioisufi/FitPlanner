package com.example.fitplannerclient.entity.profile;

public class Profile {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private final ProfileType profileType;

    public Profile(String userId, String firstName, String lastName, String email, String phoneNumber, ProfileType profileType) {
        this.userId = userId;

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profileType =  profileType;
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

    public String getContactEmail() {
        return email;
    }

    public void setContactEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public enum ProfileType{
        TRAINER,
        ATHLETE
    }

}

