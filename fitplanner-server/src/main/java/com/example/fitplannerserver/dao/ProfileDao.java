package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.User;

import java.util.List;

public interface ProfileDao {
    // Retrieves a profile by its unique ID
    User findById(String uuid);

    // Updates existing profile information
    void save(User user);

    // Retrieve the trainer assigned to a specific athlete
    User findTrainerByAthleteId(String athleteUuid);

    // Retrieve all athletes followed by a specific trainer
    List<User> findAthletesByTrainerId(String trainerUuid);

    // Find a trainer using their unique invitation code
    User findByInvitationCode(String invitationCode);

    // NEW: Create the link between the athlete and the trainer in the database
    void linkAthleteToTrainer(String athleteUuid, String trainerUuid);
}