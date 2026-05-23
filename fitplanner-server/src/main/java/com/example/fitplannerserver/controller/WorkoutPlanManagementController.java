package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutPlanSummaryDTO;
import com.example.fitplannerserver.beanvalidator.PlanValidator;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.*;
import com.example.fitplannerserver.mapper.PlanMapper;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.security.IdentityProvider;
import com.example.fitplannerserver.util.ValidationUtils;
import com.github.f4b6a3.uuid.UuidCreator;

import java.util.List;
import java.util.Optional;

public class WorkoutPlanManagementController {
    private final IdentityProvider identityProvider;

    private final WorkoutPlanDao workoutPlanDao;
    private final CoachingDao coachingDao;

    public WorkoutPlanManagementController(
            IdentityProvider identityProvider,
            WorkoutPlanDao workoutPlanDao,
            CoachingDao coachingDao
    ) {
        this.identityProvider = identityProvider;

        this.workoutPlanDao = workoutPlanDao;
        this.coachingDao = coachingDao;
    }

    public List<WorkoutPlanSummaryDTO> getMyPlansSummary() {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        try {
            return workoutPlanDao.findPlansByTrainerId(identityProvider.getUserId())
                    .stream()
                    .map(PlanMapper::toSummaryDto)
                    .toList();

        } catch (DaoException e) {
            throw new SystemException("Errore nel recuperare i WorkoutPlan creati");
        }
    }

    public WorkoutPlanDTO getPlanDetails(String planId) {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        try {
            WorkoutPlan workoutPlan = workoutPlanDao.findPlanById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan non trovato"));

            if(!workoutPlan.getAuthorId().equals(identityProvider.getUserId()))
                throw new ForbiddenException("Non puoi visualizzare i dettagli di un WorkoutPlan che non ti appartiene");

            return PlanMapper.toDto(workoutPlan);

        } catch (DaoException e) {
            throw new SystemException("Errore nel recuperare i WorkoutPlan creati");
        }
    }

    public WorkoutPlanDTO getAssignedPlan() {
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        try {
            return PlanMapper.toDto(
                    workoutPlanDao.findAssignedPlanByAthleteId(identityProvider.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Non hai un piano assegnato"))
            );

        } catch (DaoException e) {
            throw new SystemException("Errore nel recuperare il WorkoutPlan assegnato");
        }
    }

    public String createPlan(WorkoutPlanDTO planBean) {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        PlanValidator.validateWorkoutPlanBean(planBean);

        try {
            String planId = UuidCreator.getTimeOrderedEpoch().toString();
            WorkoutPlan plan = PlanMapper.toEntity(planBean, planId);

            plan.setAuthorId(identityProvider.getUserId());

            workoutPlanDao.savePlan(plan);
            return planId;

        } catch (DaoException e) {
            throw new SystemException("Errore nel creare il WorkoutPlan");
        }
    }

    public void assignPlanTo(String planId, String athleteId) {
        if (!ValidationUtils.isValidUuid(planId)) {
            throw new WrongArgumentsException("planId deve essere un UUID valido");
        }
        if (!ValidationUtils.isValidUuid(athleteId)) {
            throw new WrongArgumentsException("athleteId deve essere un UUID valido");
        }

        identityProvider.checkUserRole(Account.Role.TRAINER);
        String trainerId = identityProvider.getUserId();

        try {
            if(!coachingDao.isClientOf(trainerId, athleteId))
                throw new ForbiddenException("L'utente non è tuo cliente");

            WorkoutPlan templatePlan = workoutPlanDao.findPlanById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan non trovato"));

            if (templatePlan.getAuthorId() == null || !templatePlan.getAuthorId().equals(trainerId)) {
                throw new ForbiddenException("Non puoi assegnare un WorkoutPlan che non ti appartiene");
            }

            // un atleta può avere un solo WorkoutPlan assegnato
            Optional<WorkoutPlan> oldPlanOpt = workoutPlanDao.findAssignedPlanByAthleteId(athleteId);

            if (oldPlanOpt.isPresent()) {
                // il piano precedente dell'atleta viene eliminato
                workoutPlanDao.deletePlan(oldPlanOpt.get().getPlanId());
            }

            String newInstanceId = UuidCreator.getTimeOrderedEpoch().toString();
            WorkoutPlan athleteSpecificPlan = new WorkoutPlan(templatePlan, newInstanceId);

            athleteSpecificPlan.assignTo(athleteId);
            workoutPlanDao.savePlan(athleteSpecificPlan);

        } catch (DaoException e) {
            throw new SystemException("Errore nell'assegnare il WorkoutPlan");
        }
    }

    public void updatePlan(String planId, WorkoutPlanDTO planBean) {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        if (!ValidationUtils.isValidUuid(planId)) {
            throw new WrongArgumentsException("planId deve essere un UUID valido");
        }
        PlanValidator.validateWorkoutPlanBean(planBean);

        try {
            WorkoutPlan oldPlan = workoutPlanDao.findPlanById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan non trovato"));

            if(oldPlan.getAuthorId() == null || !oldPlan.getAuthorId().equals(identityProvider.getUserId())) {
                throw new ForbiddenException("Il WorkoutPlan non ti appartiene");
            }

            WorkoutPlan newPlan = PlanMapper.toEntity(planBean, planId);
            newPlan.setAuthorId(oldPlan.getAuthorId());
            newPlan.assignTo(oldPlan.getAssignedToId());
            newPlan.setStartDate(oldPlan.getStartDate());

            workoutPlanDao.savePlan(newPlan);

        } catch (DaoException e) {
            throw new SystemException("Errore nell'aggiornamento del piano");
        }
    }

    public void deletePlan(String planId) {
        if (!ValidationUtils.isValidUuid(planId)) {
            throw new WrongArgumentsException("planId deve essere un UUID valido");
        }

        identityProvider.checkUserRole(Account.Role.TRAINER);

        try {
            WorkoutPlan plan = workoutPlanDao.findPlanById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("WorkoutPlan non trovato"));

            if(plan.getAuthorId() == null || !plan.getAuthorId().equals(identityProvider.getUserId())) {
                throw new ForbiddenException("Il WorkoutPlan non ti appartiene");
            }

            workoutPlanDao.deletePlan(planId);
        } catch (DaoException e) {
            throw new SystemException("Errore nell'eliminazione del WorkoutPlan");
        }
    }
}
