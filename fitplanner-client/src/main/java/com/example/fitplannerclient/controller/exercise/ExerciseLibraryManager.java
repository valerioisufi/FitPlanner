package com.example.fitplannerclient.controller.exercise;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.service.api.ExerciseLibraryApi;
import com.example.fitplannerclient.util.ValidationUtils;
import com.example.fitplannercommon.ExerciseDescriptionDTO;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ExerciseLibraryManager {
    private final ExerciseLibraryApi api;

    private final Map<String, ExerciseDescription> exerciseCache;

    public ExerciseLibraryManager(ExerciseLibraryApi api) {
        this.api = api;
        this.exerciseCache = new HashMap<>();
    }

    public CompletableFuture<List<ExerciseDescriptionBean>> getExercisesAsync(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return api.getExercisesAsync(null)
                    .thenApply(fetchedDtos -> {
                        List<ExerciseDescriptionBean> beans = new ArrayList<>();
                        for (ExerciseDescriptionDTO dto : fetchedDtos) {
                            ExerciseDescription entity = dtoToEntity(dto);
                            exerciseCache.put(entity.getExerciseId(), entity);
                            beans.add(entityToBean(entity));
                        }
                        return beans;
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
            List<ExerciseDescriptionBean> beans = uuids.stream()
                    .map(exerciseCache::get)
                    .map(this::entityToBean)
                    .toList();
            return CompletableFuture.completedFuture(beans);
        }
        // richiedo alla Api solo gli UUID mancanti
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
                            .map(this::entityToBean)
                            .toList();
                });
    }

    public CompletableFuture<String> addExerciseAsync(ExerciseDescriptionBean bean) {
        try {
            validateExerciseBean(bean);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(e);
        }

        ExerciseDescription entity = beanToEntity(bean);
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

    public CompletableFuture<Void> updateExerciseAsync(ExerciseDescriptionBean bean) {
        try {
            validateExerciseBean(bean);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(e);
        }

        String exerciseId = bean.getExerciseId();

        ExerciseDescription updatedEntity = beanToEntity(bean);

        return api.updateExerciseAsync(exerciseId, entityToDto(updatedEntity))
                .thenAccept(v -> {
                    // aggiorno la cache locale se l'aggiornamento della descrizione dell'esercizio ha avuto successo
                    exerciseCache.put(exerciseId, updatedEntity);
                });
    }

    public CompletableFuture<Void> removeExerciseAsync(String exerciseId) {
        return api.removeExerciseAsync(exerciseId)
                .thenAccept(v -> {
                    // rimuovo l'elemento dalla cache solo se la rimozione remota è completata con successo
                    exerciseCache.remove(exerciseId);
                });
    }

    // validation
    private void validateExerciseBean(ExerciseDescriptionBean bean) throws IllegalArgumentException {
        String nameError = ValidationUtils.validateRequired(bean.getName(), "Nome Esercizio", 50);
        String descError = (bean.getExecution() != null && bean.getExecution().length() > 500) 
                ? "La descrizione non può superare i 500 caratteri" : null;

        if (nameError != null || descError != null) {
            throw new IllegalArgumentException("Dati dell'esercizio non validi");
        }

        if (bean.getMuscleGroups() != null) {
            for (String tag : bean.getMuscleGroups()) {
                if (tag != null && tag.length() > 50) {
                    throw new IllegalArgumentException("I gruppi muscolari non possono superare i 50 caratteri");
                }
            }
        }
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

    private ExerciseDescriptionBean entityToBean(ExerciseDescription entity) {
        ExerciseDescriptionBean bean = new ExerciseDescriptionBean();
        bean.setExerciseId(entity.getExerciseId());
        bean.setName(entity.getName());
        bean.setExecution(entity.getExecution());
        bean.setMuscleGroups(entity.getMuscleGroups().stream().toList());
        return bean;
    }

    private ExerciseDescription beanToEntity(ExerciseDescriptionBean bean) {
        return new ExerciseDescription(
                bean.getExerciseId(),
                bean.getName(),
                bean.getExecution(),
                bean.getMuscleGroups().stream().toList()
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