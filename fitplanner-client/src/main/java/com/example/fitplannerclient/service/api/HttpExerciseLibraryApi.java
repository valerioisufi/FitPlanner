package com.example.fitplannerclient.service.api;

import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannercommon.ExerciseDescriptionDTO;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HttpExerciseLibraryApi implements ExerciseLibraryApi {

    private final HttpService httpService;

    public HttpExerciseLibraryApi(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Recupera la libreria di esercizi.
     */
    @Override
    public CompletableFuture<List<ExerciseDescriptionDTO>> getExercisesAsync(List<String> uuids) {
        String url = "/exercises";
        if (uuids != null && !uuids.isEmpty()) {
            url += "?uuids=" + String.join(",", uuids);
        }
        return httpService.getAsync(url, ExerciseDescriptionDTO[].class)
                .thenApply(Arrays::asList);
    }

    /**
     * Aggiunge un nuovo esercizio alla libreria.
     */
    @Override
    public CompletableFuture<String> addExerciseAsync(ExerciseDescriptionDTO dto) {
        return httpService.postAsync("/exercises", dto, String.class);
    }

    /**
     * Aggiorna un esercizio esistente.
     */
    @Override
    public CompletableFuture<Void> updateExerciseAsync(String uuid, ExerciseDescriptionDTO dto) {
        return httpService.putAsync("/exercises/" + uuid, dto, Void.class);
    }

    /**
     * Rimuove un esercizio dalla libreria.
     */
    @Override
    public CompletableFuture<Void> removeExerciseAsync(String uuid) {
        return httpService.deleteAsync("/exercises/" + uuid, Void.class);
    }
}
