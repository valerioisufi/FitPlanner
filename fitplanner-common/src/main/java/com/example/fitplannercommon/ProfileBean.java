package com.example.fitplannercommon;

public class ProfileBean {
    private String userId; // utilizzato solo quando è il server a inviare il bean
    private String username;

    private String firstName;
    private String lastName;

    private String phoneNumber;
    private String contactEmail;

    private ProfileType profileType;

    public ProfileBean() {}

    public ProfileBean(String userId, String username, String firstName, String lastName, String phoneNumber, String contactEmail, ProfileType profileType) {
        this.userId = userId;
        this.username = username;

        this.firstName = firstName;
        this.lastName = lastName;

        this.phoneNumber = phoneNumber;
        this.contactEmail = contactEmail;

        this.profileType = profileType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public enum ProfileType {
        TRAINER,
        ATHLETE
    }

}
