package com.example.fitplannerserver.model.user;

public class AthleteUser extends User {

    private String trainerId;

    public AthleteUser(String id) {
        super(id);
    }

    public AthleteUser(String id, String firstName, String lastName, String contactEmail, String phoneNumber, String trainerId) {
        super(id, firstName, lastName, contactEmail, phoneNumber);
        this.trainerId = trainerId;
    }

    public AthleteUser(AthleteUser athleteUser) {
        super(athleteUser);
        this.trainerId = athleteUser.trainerId;
    }

    @Override
    public Account.Role getProfileType() {
        return Account.Role.ATHLETE;
    }

    @Override
    public User copy() {
        return new AthleteUser(this);
    }

    @Override
    public String getTrainerId() {
        return trainerId;
    }

    public void linkTo(TrainerUser trainer) {
        if (trainer.getId().equals(this.getId())) {
            throw new IllegalArgumentException("Non puoi collegarti a te stesso");
        }
        this.trainerId = trainer.getId();
    }

    public void unlink() {
        this.trainerId = null;
    }
}
