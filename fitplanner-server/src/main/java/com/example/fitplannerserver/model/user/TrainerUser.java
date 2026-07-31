package com.example.fitplannerserver.model.user;

public class TrainerUser extends User {
    private final String invitationCode;

    public TrainerUser(String id, String invitationCode) {
        super(id);
        this.invitationCode = invitationCode;
    }

    public TrainerUser(String id, String firstName, String lastName, String contactEmail, String phoneNumber, String invitationCode) {
        super(id, firstName, lastName, contactEmail, phoneNumber);
        this.invitationCode = invitationCode;
    }

    public TrainerUser(TrainerUser trainerUser) {
        super(trainerUser);
        this.invitationCode = trainerUser.invitationCode;
    }

    @Override
    public Account.Role getProfileType() {
        return Account.Role.TRAINER;
    }

    @Override
    public String getInvitationCode() {
        return invitationCode;
    }

    @Override
    public User copy() {
        return new TrainerUser(this);
    }

}
