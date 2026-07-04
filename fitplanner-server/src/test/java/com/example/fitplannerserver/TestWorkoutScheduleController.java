package com.example.fitplannerserver;

import com.example.fitplannercommon.WorkoutScheduleDTO;
import com.example.fitplannercommon.WorkoutState;
import com.example.fitplannerserver.controller.WorkoutScheduleController;
import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.dao.inmemory.InMemorySessionLogDao;
import com.example.fitplannerserver.dao.inmemory.InMemoryWorkoutPlanDao;
import com.example.fitplannerserver.exception.ResourceNotFoundException;
import com.example.fitplannerserver.mock.MockIdentityProvider;
import com.example.fitplannerserver.model.user.Account;
import com.example.fitplannerserver.model.log.SessionLog;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test della classe WorkoutScheduleController
 * che espone le logiche per recuperare la schedulazione del piano di allenamento
 *
 * @author Dennis Imperia
 */
class TestWorkoutScheduleController {

    private WorkoutScheduleController controller;
    private WorkoutPlanDao workoutPlanDao;
    private SessionLogDao sessionLogDao;

    private final MockIdentityProvider mockIdentityProvider = new MockIdentityProvider();

    @BeforeEach
    void setup() {
        workoutPlanDao = new InMemoryWorkoutPlanDao();
        sessionLogDao = new InMemorySessionLogDao();

        controller = new WorkoutScheduleController(
                mockIdentityProvider,
                workoutPlanDao,
                sessionLogDao
        );

        // Impostiamo un atleta di default per i test
        mockIdentityProvider.setCurrentUser("athlete-123", Account.Role.ATHLETE);
    }

    @Test
    @DisplayName("Quando l'atleta non ha piani assegnati, lancia ResourceNotFoundException")
    void testGetCurrentCycleSchedule_NoPlanAssigned() {
        assertThrows(ResourceNotFoundException.class, () -> {
            controller.getCurrentCycleSchedule();
        });
    }

    @Test
    @DisplayName("Quando il piano dell'atleta è assegnato ma la data di inizio è nel futuro, lancia ResourceNotFoundException")
    void testGetCurrentCycleSchedule_PlanNotStarted() throws Exception {
        // Arrange
        WorkoutPlan plan = new WorkoutPlan("plan-1", "My Plan", 7);
        plan.assignTo(mockIdentityProvider.getUserId());
        plan.setStartDate(LocalDate.now(ZoneOffset.UTC).plusDays(2)); // Il piano inizierà tra due giorni

        workoutPlanDao.savePlan(plan);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            controller.getCurrentCycleSchedule();
        });
        assertEquals("Piano non ancora iniziato", exception.getMessage());
    }

    @Test
    @DisplayName("Restituisce correttamente lo schedule calcolato per l'atleta nel ciclo corrente")
    void testGetCurrentCycleSchedule_Success() throws Exception {
        // Arrange
        WorkoutPlan plan = new WorkoutPlan("plan-2", "Forza e Ipertrofia", 7);
        plan.assignTo(mockIdentityProvider.getUserId());
        plan.setStartDate(LocalDate.now(ZoneOffset.UTC).minusDays(2)); // Iniziato due giorni fa (Siamo al day 2 relativo)

        WorkoutSession sessionDay0 = new WorkoutSession("Giorno 1", "{}", 0);
        WorkoutSession sessionDay2 = new WorkoutSession("Giorno 2", "{}", 2);
        WorkoutSession sessionDay4 = new WorkoutSession("Giorno 3", "{}", 4);

        plan.addSession(sessionDay0);
        plan.addSession(sessionDay2);
        plan.addSession(sessionDay4);

        workoutPlanDao.savePlan(plan);

        // Simulo che l'allenamento al day 0 è stato completato (ieri l'altro)
        SessionLog log1 = new SessionLog(
                mockIdentityProvider.getUserId(),
                "Buon allenamento",
                SessionLog.SessionStatus.COMPLETED,
                LocalDateTime.now(ZoneOffset.UTC).minusDays(2),
                plan.getPlanId(),
                0 // Si riferisce alla WorkoutSessionDay 0
        );
        sessionLogDao.saveSessionLog(log1);

        // Act
        WorkoutScheduleDTO schedule = controller.getCurrentCycleSchedule();

        // Assert
        assertNotNull(schedule, "Lo schedule non deve essere nullo");
        assertEquals("plan-2", schedule.getPlanId());
        assertEquals("Forza e Ipertrofia", schedule.getPlanTitle());
        assertEquals(2, schedule.getTodayAbsoluteDay()); // Poiché è iniziato 2 giorni fa

        // Verifico i giorni restituiti per i 7 giorni del ciclo (primo ciclo: giorni assoluti 0..6)
        assertEquals(7, schedule.getDays().size());
        assertEquals(0, schedule.getDays().get(0).getAbsoluteDay());
        assertEquals(6, schedule.getDays().get(6).getAbsoluteDay());

        // Day 0: Completato -> DONE
        assertEquals(WorkoutState.DONE, schedule.getDays().get(0).getState());
        // Day 1: Nessuna sessione nel piano -> REST
        assertEquals(WorkoutState.REST, schedule.getDays().get(1).getState());
        // Day 2: Sessione presente, nessun log -> TO_DO
        assertEquals(WorkoutState.TO_DO, schedule.getDays().get(2).getState());
        // Day 3: Nessuna sessione -> REST
        assertEquals(WorkoutState.REST, schedule.getDays().get(3).getState());
        // Day 4: Sessione presente, nessun log -> TO_DO
        assertEquals(WorkoutState.TO_DO, schedule.getDays().get(4).getState());

        // Verifico quale sia il prossimo allenamento suggerito
        assertEquals(2, schedule.getSuggestedAbsoluteDay());
    }

    @Test
    @DisplayName("Quando un trainer cerca di richiedere lo schedule, lancia RuntimeException")
    void testTrainerRequestingSchedule_ThrowsUnauthorized() {
        // Arrange
        mockIdentityProvider.setCurrentUser("trainer-123", Account.Role.TRAINER); // Cambio il ruolo del mock

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            controller.getCurrentCycleSchedule();
        });
    }
}