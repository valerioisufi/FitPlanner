package com.example.fitplannerserver.model;

public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public User(String username, String name, String surname, String email, String phoneNumber) {
        this.username = username;
        this.firstName = name;
        this.lastName = surname;
        this.email = email;
        this.phoneNumber = phoneNumber;
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
    public void setName(String name) {
        this.firstName = name;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String surname) {
        this.lastName = surname;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
