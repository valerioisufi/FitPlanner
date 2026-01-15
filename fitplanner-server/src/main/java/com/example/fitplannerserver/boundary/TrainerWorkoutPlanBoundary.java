package com.example.fitplannerserver.boundary;

import com.example.fitplannercommon.WorkoutSessionBean;
import com.example.fitplannerserver.model.WorkoutPlan;
import com.example.fitplannerserver.model.WorkoutSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trainer/workout")
public class TrainerWorkoutPlanBoundary {

    public void createWorkoutPlan(WorkoutPlan workoutPlan) {}

    public void updateWorkoutPlan(WorkoutPlan workoutPlan) {}

    public void deleteWorkoutPlan() {}

    public void getWorkoutPlan() {}

    public void getWorkoutPlanList() {}

    public void getWorkoutSessionList() {} // già in workoutPlan

    public void addOrModifySessionToWorkoutPlan(WorkoutSessionBean sessionBean) {}

    public void removeSessionFromWorkoutPlan() {}

    public void assignWorkoutPlanToAthlete() {}

    public void unassignWorkoutPlanFromAthlete() {}



}
