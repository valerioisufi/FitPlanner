package com.example.fitplannerserver.model;

public class User {
    private final String id;
    private String username;
    private String firstName;
    private String lastName;
    private String contactEmail;
    private String phoneNumber;

    private String invitationCode;

    public User(String id, String username, String name, String surname, String contactEmail, String phoneNumber, String invitationCode) {
        this.id= id;
        this.username = username;
        this.firstName = name;
        this.lastName = surname;
        this.contactEmail = contactEmail;
        this.phoneNumber = phoneNumber;
        this.invitationCode = invitationCode;
    }

    public User(User user){
        this.id = user.id;
        this.username = user.username;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.contactEmail = user.contactEmail;
        this.phoneNumber = user.phoneNumber;
        this.invitationCode = user.invitationCode;
    }

    public String getId(){return id;}

    public void setUserProfileInfo(String username, String firstName, String lastName, String contactEmail, String phoneNumber){
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactEmail = contactEmail;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }
}
