package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.ExerciseDescriptionDTO;
import com.example.fitplannerserver.beanvalidator.PlanValidator;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.exception.ForbiddenException;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.plan.ExerciseDescription;
import com.example.fitplannerserver.security.IdentityProvider;
import com.example.fitplannerserver.util.ValidationUtils;
import com.github.f4b6a3.uuid.UuidCreator;

import java.util.List;

public class ManageExerciseLibraryController {
    private final IdentityProvider identityProvider;

    private final ExerciseLibraryDao exerciseLibraryDao;
    private final CoachingDao coachingDao;

    public ManageExerciseLibraryController(
            IdentityProvider identityProvider,
            ExerciseLibraryDao exerciseLibraryDao,
            CoachingDao coachingDao
    ) {
        this.identityProvider = identityProvider;

        this.exerciseLibraryDao = exerciseLibraryDao;
        this.coachingDao = coachingDao;
    }

    public String addExercise(ExerciseDescriptionDTO exerciseBean) {
        identityProvider.checkUserRole(Account.Role.TRAINER);
        PlanValidator.validateExerciseDescriptionBean(exerciseBean);

        String newExerciseId = UuidCreator.getTimeOrderedEpoch().toString();
        ExerciseDescription newExercise = new ExerciseDescription(identityProvider.getUserId(), newExerciseId);
        newExercise.setDescription(
                exerciseBean.getName(),
                exerciseBean.getExecution(),
                exerciseBean.getMuscleGroups()
        );

        try {
            exerciseLibraryDao.saveExercise(newExercise);

        } catch (DaoException e){
            throw new SystemException("Errore nell'aggiunta dell'esercizio");
        }

        return newExerciseId;
    }

    public void updateExercise(ExerciseDescriptionDTO exerciseBean) {
        identityProvider.checkUserRole(Account.Role.TRAINER);
        PlanValidator.validateExerciseDescriptionBean(exerciseBean);
        ValidationUtils.isValidUuid(exerciseBean.getExerciseId());

        ExerciseDescription exercise = new ExerciseDescription(identityProvider.getUserId(), exerciseBean.getExerciseId());
        exercise.setDescription(
                exerciseBean.getName(),
                exerciseBean.getExecution(),
                exerciseBean.getMuscleGroups()
        );

        try {
            exerciseLibraryDao.findById(exercise.getExerciseId())
                .filter(e -> e.getTrainerId().equals(identityProvider.getUserId()))
                .orElseThrow(() -> new ForbiddenException("Esercizio non trovato o non appartenente al trainer"));

            exerciseLibraryDao.saveExercise(exercise);

        } catch (DaoException e){
            throw new SystemException("Errore nell'aggiornamento dell'esercizio");
        }

    }

    public void removeExercise(String exerciseId) {
        identityProvider.checkUserRole(Account.Role.TRAINER);
        ValidationUtils.isValidUuid(exerciseId);

        try {
            exerciseLibraryDao.findById(exerciseId)
                    .filter(e -> e.getTrainerId().equals(identityProvider.getUserId()))
                    .orElseThrow(() -> new ForbiddenException("Esercizio non trovato o non appartenente al trainer"));

            exerciseLibraryDao.deleteExercise(exerciseId);

        } catch (DaoException e){
            throw new SystemException("Errore nell'aggiornamento dell'esercizio");
        }

    }

    public List<ExerciseDescriptionDTO> getExercisesByIds(List<String> exerciseIds) {
        if(exerciseIds == null || exerciseIds.isEmpty()){
            throw new WrongArgumentsException("exerciseIds non può essere null o vuoto");
        }
        for(String id: exerciseIds){
            if(ValidationUtils.isValidUuid(id)){
                throw new WrongArgumentsException("exerciseIds devono essere UUID validi");
            }
        }

        try{
            String trainerId = switch(identityProvider.getUserRole()){
                case Account.Role.TRAINER -> identityProvider.getUserId();
                case Account.Role.ATHLETE -> coachingDao.findTrainerIdByAthleteId(identityProvider.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Atleta non associato a nessun trainer"));
            };

            return exerciseLibraryDao.findByIds(exerciseIds)
                    .stream()
                    .filter(e -> e.getTrainerId().equals(trainerId))
                    .map(e -> new ExerciseDescriptionDTO(
                            e.getExerciseId(),
                            e.getName(),
                            e.getExecution(),
                            e.getMuscleGroups()
                    ))
                    .toList();

        } catch(DaoException e){
            throw new SystemException("Errore nel recupero degli esercizi");
        }
    }

    public List<ExerciseDescriptionDTO> getLibrary() {

        try{
            String trainerId = switch(identityProvider.getUserRole()){
                case Account.Role.TRAINER -> identityProvider.getUserId();
                case Account.Role.ATHLETE -> coachingDao.findTrainerIdByAthleteId(identityProvider.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Atleta non associato a nessun trainer"));
            };

            return exerciseLibraryDao.findAllByTrainerId(trainerId)
                    .stream()
                    .map(e -> new ExerciseDescriptionDTO(
                            e.getExerciseId(),
                            e.getName(),
                            e.getExecution(),
                            e.getMuscleGroups()
                    ))
                    .toList();

        } catch(DaoException e){
            throw new SystemException("Errore nel recupero della libreria di esercizi");
        }
    }

}
