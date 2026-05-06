package com.example.fitplannerclient.entity.profile;


public class TrainerProfileClient extends ProfileClient{
    private String bio;
    private String invitationCode;

    public TrainerProfileClient(String username, String firstName, String lastName, String email, String phoneNumber, String bio, String invitationCode) {
        super(username, firstName, lastName, email, phoneNumber);
        this.bio = bio;
        this.invitationCode = invitationCode;
    }

    private String generateInvitationCode() {
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

}
