package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.ScheduleDayDTO;
import com.example.fitplannercommon.WorkoutScheduleDTO;
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

            int todayAbsoluteDay = activePlan.calculateAbsoluteDay(today);
            if (todayAbsoluteDay == -1) {
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
                    todayAbsoluteDay
            );

            buildCycleDays(schedule, activePlan, cycleLogs, today);

            return schedule;

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare lo schedule corrente", e);
        }

    }

    // Popola un elemento per ogni giorno del ciclo corrente e individua il giorno suggerito
    private void buildCycleDays(WorkoutScheduleDTO schedule, WorkoutPlan activePlan, List<SessionLog> cycleLogs, LocalDate today) {
        int absoluteCycleStartDay = activePlan.calculateAbsoluteCycleStartDay(today);
        int absoluteCycleEndDay = activePlan.calculateAbsoluteCycleEndDay(today);

        List<ScheduleDayDTO> days = new ArrayList<>();
        int suggestedAbsoluteDay = -1;

        for (int absoluteDay = absoluteCycleStartDay; absoluteDay <= absoluteCycleEndDay; absoluteDay++) {
            int cycleDay = absoluteDay % activePlan.getCycleLength();
            WorkoutSession template = activePlan.getSession(cycleDay);

            if (template == null) {
                days.add(new ScheduleDayDTO(absoluteDay, WorkoutState.REST));
                continue;
            }

            SessionLog.SessionStatus sessionStatus = findSessionStateForDay(cycleLogs, absoluteDay);
            WorkoutState workoutState = toWorkoutState(sessionStatus);
            days.add(new ScheduleDayDTO(absoluteDay, workoutState));

            if (suggestedAbsoluteDay == -1 && (workoutState == WorkoutState.TO_DO || workoutState == WorkoutState.IN_PROGRESS)) {
                suggestedAbsoluteDay = absoluteDay;
            }

        }

        schedule.setDays(days);
        schedule.setSuggestedAbsoluteDay(suggestedAbsoluteDay);
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