package com.example.fitplannerserver.api;

import com.example.fitplannercommon.WorkoutPlanBean;
import com.example.fitplannercommon.WorkoutScheduleBean;
import com.example.fitplannerserver.controller.WorkoutPlanManagementController;
import com.example.fitplannerserver.controller.WorkoutScheduleController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan")
public class WorkoutPlanBoundary {

    private final WorkoutPlanManagementController workoutPlanManagementController;
    private final WorkoutScheduleController workoutScheduleController;

    public WorkoutPlanBoundary(WorkoutPlanManagementController workoutPlanManagementController, WorkoutScheduleController workoutScheduleController) {
        this.workoutPlanManagementController = workoutPlanManagementController;
        this.workoutScheduleController = workoutScheduleController;
    }

    // Fetch all workout plans created by the trainer
    @GetMapping
    public List<WorkoutPlanBean> getMyCreatedPlans() {
        return workoutPlanManagementController.getMyPlans();
    }

    // Athlete fetches their currently assigned workout plan
    @GetMapping("/assigned")
    public WorkoutPlanBean getAssignedPlan() {
        return workoutPlanManagementController.getAssignedPlan();
    }

    @GetMapping("/schedule")
    public WorkoutScheduleBean getCurrentCycleSchedule() {
        return workoutScheduleController.getCurrentCycleSchedule();
    }

    // Creates a new, empty workout plan. Returns the generated UUID.
    @PostMapping
    public String createPlan(@RequestBody WorkoutPlanBean planBean) {
        return workoutPlanManagementController.createPlan(planBean);
    }

    // Assigns a workout plan to a specific athlete using their UUID
    @PostMapping("/{planId}/assign/{athleteId}")
    public void assignPlanTo(@PathVariable String planId, @PathVariable String athleteId) {
        workoutPlanManagementController.assignPlanTo(planId, athleteId);
    }

    // Updates the general content/title of a workout plan
    @PutMapping("/{planId}")
    public void updatePlan(@PathVariable String planId, @RequestBody WorkoutPlanBean planBean) {
        workoutPlanManagementController.updatePlan(planId, planBean);
    }

    // Deletes an entire workout plan
    @DeleteMapping("/{planId}")
    public void deletePlan(@PathVariable String planId) {
        workoutPlanManagementController.deletePlan(planId);
    }
}