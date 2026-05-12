package com.example.fitplannerserver.model;

import java.util.List;

public class Trainer extends User {
    private List<WorkoutPlan> workoutPlans;

    public Trainer(String username, String name, String surname, String email, String phoneNumber) {
        super(username, name, surname, email, phoneNumber);
    }
}
