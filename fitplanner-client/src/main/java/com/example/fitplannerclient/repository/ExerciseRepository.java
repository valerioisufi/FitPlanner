package com.example.fitplannerclient.repository;

import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.service.api.ExerciseLibraryApi;
import com.example.fitplannercommon.ExerciseDescriptionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ExerciseRepository {
    private final ExerciseLibraryApi api;

    private final Map<String, ExerciseDescription> exerciseCache;
    private volatile boolean isCacheComplete = false;

    public ExerciseRepository(ExerciseLibraryApi api) {
        this.api = api;
        this.exerciseCache = new ConcurrentHashMap<>();
    }

    public ExerciseDescription getCachedExercise(String uuid) {
        return exerciseCache.get(uuid);
    }

    public CompletableFuture<List<ExerciseDescription>> getExercisesAsync(List<String> uuids) {
        if (isCacheComplete) {
            // la cache è completa, non richiedo i dati alle api
            if (uuids == null || uuids.isEmpty()) {
                return CompletableFuture.completedFuture(
                        exerciseCache.values().stream().toList()
                );

            } else {
                return CompletableFuture.completedFuture(
                        uuids.stream()
                                .map(exerciseCache::get)
                                .filter(Objects::nonNull)
                                .toList()
                );
            }
        }

        if (uuids == null || uuids.isEmpty()) {
            return api.getExercisesAsync(null)
                    .thenApply(fetchedDtos -> {
                        List<ExerciseDescription> entities = new ArrayList<>();

                        for (ExerciseDescriptionDTO dto : fetchedDtos) {
                            ExerciseDescription entity = dtoToEntity(dto);
                            exerciseCache.put(entity.getExerciseId(), entity);
                            entities.add(entity);
                        }

                        isCacheComplete = true;
                        return entities;
                    });
        }

        List<String> missingUuids = new ArrayList<>();
        // identifico quali UUID mancano dalla cache
        for (String uuid : uuids) {
            if (!exerciseCache.containsKey(uuid)) {
                missingUuids.add(uuid);
            }
        }
        // se non manca nulla, restituisco subito i Bean creati dalla cache
        if (missingUuids.isEmpty()) {
            List<ExerciseDescription> entities = uuids.stream()
                    .map(exerciseCache::get)
                    .toList();
            return CompletableFuture.completedFuture(entities);
        }
        // richiedo all'Api solo gli UUID mancanti
        return api.getExercisesAsync(missingUuids)
                .thenApply(fetchedDtos -> {
                    // aggiorno la cache locale con le nuove entità arrivate dal server
                    fetchedDtos.forEach(dto -> {
                        ExerciseDescription entity = dtoToEntity(dto);
                        exerciseCache.put(entity.getExerciseId(), entity);
                    });
                    // costruisco la lista finale convertita in Bean
                    return uuids.stream()
                            .map(exerciseCache::get)
                            .filter(Objects::nonNull)
                            .toList();
                });
    }

    public CompletableFuture<String> addExerciseAsync(ExerciseDescription entity) {
        ExerciseDescriptionDTO dto = entityToDto(entity);

        return api.addExerciseAsync(dto)
                .thenApply(newId -> {
                    ExerciseDescription newEntity = new ExerciseDescription(
                            newId,
                            entity.getName(),
                            entity.getExecution(),
                            entity.getMuscleGroups()
                    );
                    exerciseCache.put(newId, newEntity);
                    return newId;
                });
    }

    public CompletableFuture<Void> updateExerciseAsync(ExerciseDescription entity) {

        return api.updateExerciseAsync(entity.getExerciseId(), entityToDto(entity))
                .thenAccept(v ->
                    // aggiorno la cache locale se l'aggiornamento della descrizione dell'esercizio ha avuto successo
                    exerciseCache.put(entity.getExerciseId(), entity)
                );
    }

    public CompletableFuture<Void> removeExerciseAsync(String exerciseId) {
        return api.removeExerciseAsync(exerciseId)
                .thenAccept(v ->
                    // rimuovo l'elemento dalla cache solo se la rimozione remota è completata con successo
                    exerciseCache.remove(exerciseId)
                );
    }

    // mapper
    private ExerciseDescription dtoToEntity(ExerciseDescriptionDTO dto) {
        return new ExerciseDescription(
                dto.getExerciseId(),
                dto.getName(),
                dto.getExecution(),
                dto.getMuscleGroups().stream().toList()
        );
    }

    private ExerciseDescriptionDTO entityToDto(ExerciseDescription entity) {
        ExerciseDescriptionDTO dto = new ExerciseDescriptionDTO();
        dto.setExerciseId(entity.getExerciseId());
        dto.setName(entity.getName());
        dto.setExecution(entity.getExecution());
        dto.setMuscleGroups(entity.getMuscleGroups().stream().toList());
        return dto;
    }
}
