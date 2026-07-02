package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.WorkoutScheduleDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import com.example.fitplannercommon.WorkoutState;
import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.ResourceNotFoundException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.log.SessionLog;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;
import com.example.fitplannerserver.security.IdentityProvider;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class WorkoutScheduleController {
    private final IdentityProvider identityProvider;

    private final WorkoutPlanDao workoutPlanDao;
    private final SessionLogDao sessionLogDao;

    public WorkoutScheduleController(
            IdentityProvider identityProvider,
            WorkoutPlanDao workoutPlanDao,
            SessionLogDao sessionLogDao
    ) {
        this.identityProvider = identityProvider;

        this.workoutPlanDao = workoutPlanDao;
        this.sessionLogDao = sessionLogDao;
    }

    public WorkoutScheduleDTO getCurrentCycleSchedule() {
        identityProvider.checkUserRole(Account.Role.ATHLETE);
        String athleteId = identityProvider.getUserId();

        try{
            WorkoutPlan activePlan = workoutPlanDao.findAssignedPlanByAthleteId(athleteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nessun piano assegnato"));

            LocalDate today = LocalDate.now(ZoneOffset.UTC);

            int currentDay = activePlan.calculateAbsoluteDay(today);
            if (currentDay == -1) {
                throw new ResourceNotFoundException("Piano non ancora iniziato");
            }

            long startMillis = toEpochMilli(activePlan.calculateCycleStartDate(today));
            long endMillis = toEpochMilli(activePlan.calculateCycleEndDate(today));

            List<SessionLog> cycleLogs = sessionLogDao.findLogsByAthleteIdAndDateRange(
                    athleteId, startMillis, endMillis
            );

            WorkoutScheduleDTO schedule = new WorkoutScheduleDTO(
                    activePlan.getPlanId(),
                    activePlan.getTitle(),
                    startMillis,
                    endMillis,
                    currentDay
            );

            buildCycleStates(schedule, activePlan, cycleLogs, today);

            return schedule;

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare lo schedule corrente", e);
        }

    }

    // Popola gli stati di ogni giorno del ciclo corrente e individua la prossima sessione suggerita
    private void buildCycleStates(WorkoutScheduleDTO schedule, WorkoutPlan activePlan, List<SessionLog> cycleLogs, LocalDate today) {
        int absoluteCycleStartDay = activePlan.calculateAbsoluteCycleStartDay(today);
        int absoluteCycleEndDay = activePlan.calculateAbsoluteCycleEndDay(today);

        List<WorkoutState> states = new ArrayList<>();
        WorkoutSessionDTO nextSuggested = null;

        for (int absoluteDay = absoluteCycleStartDay; absoluteDay <= absoluteCycleEndDay; absoluteDay++) {
            int relativeDayInCycle = absoluteDay % activePlan.getCycleLength();
            WorkoutSession template = activePlan.getSession(relativeDayInCycle);

            if (template == null) {
                states.add(WorkoutState.REST);
                continue;
            }

            SessionLog.SessionStatus sessionStatus = findSessionStateForDay(cycleLogs, absoluteDay);
            WorkoutState workoutState = toWorkoutState(sessionStatus);
            states.add(workoutState);

            if (nextSuggested == null && (workoutState == WorkoutState.TO_DO || workoutState == WorkoutState.IN_PROGRESS)) {
                    nextSuggested = new WorkoutSessionDTO(template.getTitle(), template.getContent(), absoluteDay);
                }

        }

        schedule.setWorkoutStates(states);
        schedule.setNextSuggestedSession(nextSuggested);
    }

    // Traduce lo stato di una sessione nello stato esposto al client.
    // L'assenza di log indica una sessione ancora da svolgere
    private WorkoutState toWorkoutState(SessionLog.SessionStatus sessionStatus) {
        if (sessionStatus == null) {
            return WorkoutState.TO_DO;
        }

        return switch (sessionStatus) {
            case SKIPPED -> WorkoutState.SKIPPED;
            case COMPLETED -> WorkoutState.DONE;
            case INTERRUPTED -> WorkoutState.IN_PROGRESS;
        };
    }

    private long toEpochMilli(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    private SessionLog.SessionStatus findSessionStateForDay(List<SessionLog> logs, int absoluteDay) {

        for (SessionLog log : logs) {
            if (log.getWorkoutSessionDay() == absoluteDay) {
                return log.getStatus();
            }

        }

        return null;
    }
}