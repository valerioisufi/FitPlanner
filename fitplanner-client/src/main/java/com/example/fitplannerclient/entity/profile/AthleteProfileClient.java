package com.example.fitplannerclient.entity.profile;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;

public class AthleteProfileClient extends ProfileClient{
    private WorkoutPlan plan;

    public AthleteProfileClient (String username, String firstName, String lastName, String email, String phoneNumber, WorkoutPlan plan){
        super(username, firstName, lastName, email, phoneNumber);
        this.plan= plan;
    }
}
