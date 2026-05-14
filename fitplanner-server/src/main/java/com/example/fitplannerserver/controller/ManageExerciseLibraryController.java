package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.ExerciseDescriptionBean;
import com.example.fitplannerserver.security.IdentityProvider;
import com.github.f4b6a3.uuid.UuidCreator;

import java.util.List;

public class ManageExerciseLibraryController {
    private final IdentityProvider identityProvider;

    public ManageExerciseLibraryController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public String addExercise(ExerciseDescriptionBean exerciseBean) {
        String trainerId = identityProvider.getUserId();

        // Generate a new UUIDv7 for the exercise
        String newExerciseUuid = UuidCreator.getTimeOrderedEpoch().toString();

        // Map the bean to an entity and save it via DAO

        return newExerciseUuid;
    }

    public void updateExercise(String uuid, ExerciseDescriptionBean exerciseBean) {
        // Update logic
    }

    public void removeExercise(String uuid) {
        // Removal logic
    }

    public List<ExerciseDescriptionBean> getExercisesByIds(List<String> uuids) {
    }

    public List<ExerciseDescriptionBean> getLibrary() {

    }
}
