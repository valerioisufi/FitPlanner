package com.example.fitplannerserver;

import com.example.fitplannercommon.WorkoutPlanBean;
import com.example.fitplannerserver.controller.WorkoutPlanController;
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
    public WorkoutPlanBean getWorkoutPlanBoundary() {
        WorkoutPlanBean workoutPlanBean = new WorkoutPlanBean();
        workoutPlanBean.setName("Workout Plan Boundary");
        workoutPlanBean.setDescription("Workout Plan Boundary");
        workoutPlanBean.setWorkoutSessions(null);

        return workoutPlanBean;
//        return workoutPlanController.retrieveWorkoutPlan();
    }

}
