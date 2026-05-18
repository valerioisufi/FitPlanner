package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.WorkoutScheduleBean;
import com.example.fitplannercommon.WorkoutSessionBean;
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
import java.time.temporal.ChronoUnit;
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

    public WorkoutScheduleBean getCurrentCycleSchedule() {
        identityProvider.checkUserRole(Account.Role.ATHLETE);
        String athleteId = identityProvider.getUserId();

        try{
            WorkoutPlan activePlan = workoutPlanDao.findAssignedPlanByAthleteId(athleteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nessun piano assegnato"));

            LocalDate today = LocalDate.now();

            int currentDay = activePlan.calculateCurrentCycleDay(today);
            LocalDate cycleStart = activePlan.calculateCycleStartDate(today);
            LocalDate cycleEnd = activePlan.calculateCycleEndDate(today);

            if (currentDay == -1) {
                throw new ResourceNotFoundException("Piano non ancora iniziato");
            }

            long startMillis = cycleStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            long endMillis = cycleEnd.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

            List<SessionLog> cycleLogs = sessionLogDao.findLogsByAthleteIdAndDateRange(
                    athleteId, startMillis, endMillis
            );

            WorkoutScheduleBean schedule = new WorkoutScheduleBean(
                    activePlan.getPlanId(),
                    activePlan.getTitle(),
                    startMillis,
                    endMillis,
                    currentDay
            );

            List<WorkoutState> states = new ArrayList<>();
            WorkoutSessionBean nextSuggested = null;
            boolean foundNextSuggested = false;

            long daysElapsedSinceStart = ChronoUnit.DAYS.between(activePlan.getStartDate(), today);
            long currentCycleIndex = daysElapsedSinceStart / activePlan.getCycleLength();

            int absoluteCycleStartDay = (int) (currentCycleIndex * activePlan.getCycleLength());
            int absoluteCycleEndDay = absoluteCycleStartDay + activePlan.getCycleLength() - 1;

            for (int absoluteDay = absoluteCycleStartDay; absoluteDay <= absoluteCycleEndDay; absoluteDay++) {

                SessionLog.SessionStatus sessionStatus = findSessionStateForDay(cycleLogs, absoluteDay);
                int relativeDayInCycle = absoluteDay % activePlan.getCycleLength();

                WorkoutSession template = activePlan.getSession(relativeDayInCycle);

                if (activePlan.getSession(relativeDayInCycle) == null) {
                    states.add(WorkoutState.REST);
                    continue;
                }

                if (sessionStatus == null) {
                    states.add(WorkoutState.TO_DO);

                    if (!foundNextSuggested) {
                        nextSuggested = new WorkoutSessionBean(
                                template.getTitle(),
                                template.getContent(),
                                relativeDayInCycle
                        );
                        foundNextSuggested = true;
                    }

                } else {
                    WorkoutState workoutState = switch (sessionStatus) {
                        case SKIPPED -> WorkoutState.SKIPPED;
                        case COMPLETED -> WorkoutState.DONE;
                        case INTERRUPTED -> WorkoutState.IN_PROGRESS;
                    };

                    states.add(workoutState);

                    if (workoutState == WorkoutState.IN_PROGRESS && !foundNextSuggested) {
                        nextSuggested = new WorkoutSessionBean(
                                template.getTitle(),
                                template.getContent(),
                                absoluteDay
                        );
                        foundNextSuggested = true;
                    }

                }

            }

            schedule.setWorkoutStates(states);
            schedule.setNextSuggestedSession(nextSuggested);

            return schedule;

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare lo schedule corrente");
        }

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