package com.example.fitplannerclient.controller.exercise;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.exception.InvalidInputException;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.util.ValidationUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExerciseLibraryManager {
    private final ExerciseRepository repository;

    public ExerciseLibraryManager(ExerciseRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<ExerciseDescriptionBean>> getExercisesAsync(List<String> uuids) {
        return repository.getExercisesAsync(uuids)
                .thenApply(entities -> entities.stream()
                        .map(this::entityToBean)
                        .toList());
    }

    public CompletableFuture<String> addExerciseAsync(ExerciseDescriptionBean bean) {
        try {
            validateExerciseBean(bean);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(e);
        }

        ExerciseDescription entity = beanToEntity(bean);
        return repository.addExerciseAsync(entity);
    }

    public CompletableFuture<Void> updateExerciseAsync(ExerciseDescriptionBean bean) {
        try {
            validateExerciseBean(bean);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(e);
        }

        ExerciseDescription updatedEntity = beanToEntity(bean);
        return repository.updateExerciseAsync(updatedEntity);
    }

    public CompletableFuture<Void> removeExerciseAsync(String exerciseId) {
        return repository.removeExerciseAsync(exerciseId);
    }

    // validation
    private void validateExerciseBean(ExerciseDescriptionBean bean) {
        String nameError = ValidationUtils.validateRequired(bean.getName(), "Nome Esercizio", 50);
        String descError = (bean.getExecution() != null && bean.getExecution().length() > 500) 
                ? "La descrizione non può superare i 500 caratteri" : null;

        if (nameError != null || descError != null) {
            throw new InvalidInputException("Dati dell'esercizio non validi");
        }

        if (bean.getMuscleGroups() != null) {
            for (String tag : bean.getMuscleGroups()) {
                if (tag != null && tag.length() > 50) {
                    throw new InvalidInputException("I gruppi muscolari non possono superare i 50 caratteri");
                }
            }
        }
    }

    // mapper
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
}