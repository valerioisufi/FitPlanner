package com.example.fitplannerserver.model;

import java.util.List;

public class Athlete extends User {
    private int weight;
    private int height;
    private WorkoutPlan workoutPlan;

    public int getWeight(){
        return weight;
    }
    public void setWeight(int weight){
        this.weight = weight;
    }

    public int getHeight(){
        return height;
    }
    public void setHeight(int height){
        this.height = height;
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
