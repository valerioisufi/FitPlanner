package com.example.fitplannerserver.model.user;

public abstract class User {
    private final String id;
    private String firstName;
    private String lastName;
    private String contactEmail;
    private String phoneNumber;

    protected User(String id) {
        this.id = id;
    }

    protected User(String id, String firstName, String lastName, String contactEmail, String phoneNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactEmail = contactEmail;
        this.phoneNumber = phoneNumber;
    }

    protected User(User user) {
        this.id = user.id;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.contactEmail = user.contactEmail;
        this.phoneNumber = user.phoneNumber;
    }

    public abstract Account.Role getProfileType();

    public abstract User copy();

    public String getId() {
        return id;
    }

    public void setUserProfileInfo(String firstName, String lastName, String contactEmail, String phoneNumber) {
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";
        this.contactEmail = contactEmail != null ? contactEmail.trim() : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber.trim() : "";
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
        return null;
    }

    public String getTrainerId() {
        return null;
    }
}
