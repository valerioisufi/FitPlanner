package com.example.fitplannerserver.api;

import com.example.fitplannercommon.WorkoutPlanBean;
import com.example.fitplannercommon.WorkoutSessionBean;
import com.example.fitplannerserver.controller.EditWorkoutPlanController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan")
public class WorkoutPlanBoundary {

    private final EditWorkoutPlanController editWorkoutPlanController;

    public WorkoutPlanBoundary(EditWorkoutPlanController editWorkoutPlanController) {
        this.editWorkoutPlanController = editWorkoutPlanController;
    }

    // Fetch all workout plans created by the trainer
    @GetMapping
    public List<WorkoutPlanBean> getMyCreatedPlans() {
        return editWorkoutPlanController.getMyPlans();
    }

    // Athlete fetches their currently assigned workout plan
    @GetMapping("/assigned")
    public WorkoutPlanBean getAssignedPlan() {
        return editWorkoutPlanController.getAssignedPlan();
    }

    // Creates a new, empty workout plan. Returns the generated UUID.
    @PostMapping
    public String createPlan(@RequestBody WorkoutPlanBean planBean) {
        return editWorkoutPlanController.createPlan(planBean);
    }

    // Assigns a workout plan to a specific athlete using their UUID
    @PostMapping("/{planUuid}/assign/{athleteUuid}")
    public void assignPlanTo(@PathVariable String planUuid, @PathVariable String athleteUuid) {
        editWorkoutPlanController.assignPlanTo(planUuid, athleteUuid);
    }

    // Updates the general content/title of a workout plan
    @PutMapping("/{planUuid}")
    public void updatePlan(@PathVariable String planUuid, @RequestBody String content) {
        editWorkoutPlanController.updatePlan(planUuid, content);
    }

    // Deletes an entire workout plan
    @DeleteMapping("/{planUuid}")
    public void deletePlan(@PathVariable String planUuid) {
        editWorkoutPlanController.deletePlan(planUuid);
    }

    // Adds a new session to an existing workout plan
    @PostMapping("/{planUuid}/sessions")
    public void addSession(@PathVariable String planUuid, @RequestBody WorkoutSessionBean sessionBean) {
        editWorkoutPlanController.addSession(planUuid, sessionBean);
    }

    // Updates a specific session (content, exercises, relative days)
    @PutMapping("/sessions/{sessionUuid}")
    public void updateSession(@PathVariable String sessionUuid, @RequestBody WorkoutSessionBean sessionBean) {
        editWorkoutPlanController.updateSession(sessionUuid, sessionBean);
    }

    // Update workout session status (e.g., athlete marks as COMPLETED)
    @PatchMapping("/sessions/{sessionUuid}/status")
    public void updateSessionStatus(@PathVariable String sessionUuid, @RequestBody SessionStatusBean statusBean) {
        editWorkoutPlanController.updateSessionStatus(sessionUuid, statusBean);
    }

    // Removes a session
    @DeleteMapping("/sessions/{sessionUuid}")
    public void deleteSession(@PathVariable String sessionUuid) {
        editWorkoutPlanController.deleteSession(sessionUuid);
    }
}