package com.example.fitplannerclient.service.api;

import com.example.fitplannercommon.ExerciseDescriptionDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExerciseLibraryApi {
    CompletableFuture<List<ExerciseDescriptionDTO>> getExercisesAsync(List<String> uuids);
    CompletableFuture<String> addExerciseAsync(ExerciseDescriptionDTO dto);
    CompletableFuture<Void> updateExerciseAsync(String uuid, ExerciseDescriptionDTO dto);
    CompletableFuture<Void> removeExerciseAsync(String uuid);
}
