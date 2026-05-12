package com.example.fitplannercommon;

public class ProfileBean {
    private String username;

    private String firstName;
    private String lastName;

    private String phoneNumber;
    private String email;

    public ProfileBean() {}

    public ProfileBean(String username, String firstName, String lastName, String phoneNumber, String email) {
        this.username = username;

        this.firstName = firstName;
        this.lastName = lastName;

        this.phoneNumber = phoneNumber;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
