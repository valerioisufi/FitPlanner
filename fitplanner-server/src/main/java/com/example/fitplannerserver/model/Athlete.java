package com.example.fitplannerserver.model;

import java.util.List;

public class Athlete extends User {
    private int weight;
    private int height;
    private List<WorkoutPlan> workoutPlans;

    public int getWeight (){
        return weight;
    }
    public void setWeight (int weight){
        this.weight = weight;
    }

    public int getHeight (){
        return height;
    }
    public void setHeight (int height){
        this.height = height;
    }

    public List<WorkoutPlan> getWorkoutPlans (){
        return workoutPlans;
    }

    public void setWorkoutPlans (List<WorkoutPlan> workoutPlans){
        this.workoutPlans = workoutPlans;
    }
    public void addWorkoutPlan(WorkoutPlan plan){
        workoutPlans.add(plan);
    }
    public void removeWorkoutPlan(WorkoutPlan plan){
        workoutPlans.remove(plan);
    }
}
