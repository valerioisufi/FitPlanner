package com.example.fitplannerserver.api;

import com.example.fitplannercommon.ExerciseDescriptionBean;
import com.example.fitplannerserver.controller.ManageExerciseLibraryController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
public class ExerciseLibraryBoundary {

    private final ManageExerciseLibraryController manageExerciseLibraryController;

    public ExerciseLibraryBoundary(ManageExerciseLibraryController manageExerciseLibraryController) {
        this.manageExerciseLibraryController = manageExerciseLibraryController;
    }

    // Retrieve the exercise library.
    // If Trainer -> returns their library. If Athlete -> returns their Trainer's library.
    // Supports an optional list of UUIDs to filter the results.
    @GetMapping
    public List<ExerciseDescriptionBean> getExercises(
            @RequestParam(required = false) List<String> uuids) {

        // If the client passed specific UUIDs, fetch only those
        if (uuids != null && !uuids.isEmpty()) {
            return manageExerciseLibraryController.getExercisesByIds(uuids);
        }

        // Otherwise, fetch the entire library
        return manageExerciseLibraryController.getLibrary();
    }

    // Adds a new exercise to the trainer's library
    @PostMapping
    public String addExercise(@RequestBody ExerciseDescriptionBean exerciseBean) {
        return manageExerciseLibraryController.addExercise(exerciseBean);
    }

    // Updates an existing exercise in the library
    @PutMapping("/{uuid}")
    public void updateExercise(@PathVariable String uuid, @RequestBody ExerciseDescriptionBean exerciseBean) {
        manageExerciseLibraryController.updateExercise(uuid, exerciseBean);
    }

    // Removes an exercise from the library
    @DeleteMapping("/{uuid}")
    public void removeExercise(@PathVariable String uuid) {
        manageExerciseLibraryController.removeExercise(uuid);
    }
}