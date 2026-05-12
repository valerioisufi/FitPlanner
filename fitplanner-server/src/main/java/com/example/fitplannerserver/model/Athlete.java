package com.example.fitplannerserver.model;

public class Athlete extends User {
    private WorkoutPlan workoutPlan;

    public Athlete(String username, String name, String surname, String email, String phoneNumber) {
        super(username, name, surname, email, phoneNumber);
    }

    public WorkoutPlan getWorkoutPlan(int id){
        return workoutPlan;
    }
    public WorkoutPlan getWorkoutPlan(){
        return workoutPlan;
    }

    public void setWorkoutPlan(WorkoutPlan workoutPlan){
        this.workoutPlan = workoutPlan;
    }
    public void removeWorkoutPlan(){
        this.workoutPlan = null;
    }


}
