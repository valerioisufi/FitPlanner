package com.example.fitplannerserver;

import com.example.fitplannerserver.controller.WorkoutPlanController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/athlete/workout")
public class AthleteWorkoutPlanBoundary {
    private final WorkoutPlanController workoutPlanController;

    public AthleteWorkoutPlanBoundary(WorkoutPlanController workoutPlanController) {
        this.workoutPlanController = workoutPlanController;
    }

    @GetMapping
    public String getWorkoutPlanBoundary() {
        return "Workout Plan Boundary";
//        return workoutPlanController.retrieveWorkoutPlan();
    }

}
