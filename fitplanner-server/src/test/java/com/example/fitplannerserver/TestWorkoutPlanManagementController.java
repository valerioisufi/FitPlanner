package com.example.fitplannerserver;

import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutPlanSummaryDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import com.example.fitplannerserver.controller.WorkoutPlanManagementController;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.dao.inmemory.InMemoryCoachingDao;
import com.example.fitplannerserver.dao.inmemory.InMemoryWorkoutPlanDao;
import com.example.fitplannerserver.exception.ForbiddenException;
import com.example.fitplannerserver.mock.MockIdentityProvider;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test della classe WorkoutPlanManagementController
 * che espone le logiche per la gestione dei piani di allenamento (CRUD + assegnazione)
 *
 * @author Dennis Imperia
 */
class TestWorkoutPlanManagementController {

    private WorkoutPlanManagementController controller;
    private WorkoutPlanDao workoutPlanDao;
    private CoachingDao coachingDao;

    private final MockIdentityProvider mockIdentityProvider = new MockIdentityProvider();

    @BeforeEach
    void setup() {
        workoutPlanDao = new InMemoryWorkoutPlanDao();
        coachingDao = new InMemoryCoachingDao();

        controller = new WorkoutPlanManagementController(
                mockIdentityProvider,
                workoutPlanDao,
                coachingDao
        );

        // Impostiamo un trainer di default per i test
        mockIdentityProvider.setCurrentUser("trainer-1", Account.Role.TRAINER);
    }

    @Test
    @DisplayName("Trainer recupera il riepilogo dei suoi piani")
    void testGetMyPlansSummary() throws Exception {
        // Arrange
        WorkoutPlan plan1 = new WorkoutPlan(UUID.randomUUID().toString(), "Piano A", 7);
        plan1.setAuthorId(mockIdentityProvider.getUserId());
        WorkoutPlan plan2 = new WorkoutPlan(UUID.randomUUID().toString(), "Piano B", 14);
        plan2.setAuthorId(mockIdentityProvider.getUserId());

        // Piano di un altro trainer
        WorkoutPlan plan3 = new WorkoutPlan(UUID.randomUUID().toString(), "Piano C", 7);
        plan3.setAuthorId("trainer-2");

        workoutPlanDao.savePlan(plan1);
        workoutPlanDao.savePlan(plan2);
        workoutPlanDao.savePlan(plan3);

        // Act
        List<WorkoutPlanSummaryDTO> summaries = controller.getMyPlansSummary();

        // Assert
        assertEquals(2, summaries.size(), "Dovrebbe restituire solo i piani dell'autore corrente");
        assertTrue(summaries.stream().anyMatch(p -> p.getPlanTitle().equals("Piano A")));
        assertTrue(summaries.stream().anyMatch(p -> p.getPlanTitle().equals("Piano B")));
    }

    @Test
    @DisplayName("Trainer recupera i dettagli di un suo piano")
    void testGetPlanDetails_Success() throws Exception {
        // Arrange
        String planId = UUID.randomUUID().toString();
        WorkoutPlan plan = new WorkoutPlan(planId, "Piano Speciale", 7);
        plan.setAuthorId(mockIdentityProvider.getUserId());
        workoutPlanDao.savePlan(plan);

        // Act
        WorkoutPlanDTO dto = controller.getPlanDetails(planId);

        // Assert
        assertNotNull(dto);
        assertEquals("Piano Speciale", dto.getName());
    }

    @Test
    @DisplayName("Trainer tenta di recuperare i dettagli di un piano altrui -> Forbidden")
    void testGetPlanDetails_Forbidden() throws Exception {
        // Arrange
        String planId = UUID.randomUUID().toString();
        WorkoutPlan plan = new WorkoutPlan(planId, "Piano Altrui", 7);
        plan.setAuthorId("altro-trainer"); // autore diverso
        workoutPlanDao.savePlan(plan);

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            controller.getPlanDetails(planId);
        });
    }

    @Test
    @DisplayName("Atleta recupera il piano a lui assegnato")
    void testGetAssignedPlan_Success() throws Exception {
        // Arrange
        mockIdentityProvider.setCurrentUser("athlete-1", Account.Role.ATHLETE);

        String planId = UUID.randomUUID().toString();
        WorkoutPlan plan = new WorkoutPlan(planId, "Piano Assegnato", 7);
        plan.setAuthorId("trainer-1");
        plan.assignTo(mockIdentityProvider.getUserId());
        workoutPlanDao.savePlan(plan);

        // Act
        WorkoutPlanDTO dto = controller.getAssignedPlan();

        // Assert
        assertNotNull(dto);
        assertEquals("Piano Assegnato", dto.getName());
    }

    @Test
    @DisplayName("Trainer crea un nuovo piano")
    void testCreatePlan_Success() throws Exception {
        // Arrange
        WorkoutPlanDTO planBean = new WorkoutPlanDTO();
        planBean.setName("Nuovo Piano");
        planBean.setCycleLength(7);
        planBean.setWorkoutSessions(List.of(
                new WorkoutSessionDTO("Giorno 1", "{}", 0)
        ));

        // Act
        String newPlanId = controller.createPlan(planBean);

        // Assert
        assertNotNull(newPlanId);
        Optional<WorkoutPlan> savedOpt = workoutPlanDao.findPlanById(newPlanId);
        assertTrue(savedOpt.isPresent());
        assertEquals("Nuovo Piano", savedOpt.get().getTitle());
        assertEquals(mockIdentityProvider.getUserId(), savedOpt.get().getAuthorId());
    }

    @Test
    @DisplayName("Trainer assegna un piano a un suo cliente")
    void testAssignPlanTo_Success() throws Exception {
        // Arrange
        String planId = UUID.randomUUID().toString();
        WorkoutPlan templatePlan = new WorkoutPlan(planId, "Template Plan", 7);
        templatePlan.setAuthorId(mockIdentityProvider.getUserId());
        workoutPlanDao.savePlan(templatePlan);

        String athleteId = UUID.randomUUID().toString();
        // L'atleta è cliente di questo trainer
        coachingDao.linkAthleteToTrainer(athleteId, mockIdentityProvider.getUserId());

        // Act
        controller.assignPlanTo(planId, athleteId);

        // Assert
        Optional<WorkoutPlan> assignedPlanOpt = workoutPlanDao.findAssignedPlanByAthleteId(athleteId);
        assertTrue(assignedPlanOpt.isPresent(), "L'atleta deve avere un piano assegnato");
        assertNotEquals(planId, assignedPlanOpt.get().getPlanId(), "Deve essere creata una copia con nuovo ID");
        assertEquals("Template Plan", assignedPlanOpt.get().getTitle());
        assertEquals(mockIdentityProvider.getUserId(), assignedPlanOpt.get().getAuthorId());
    }

    @Test
    @DisplayName("Trainer assegna un piano a un atleta che NON è suo cliente -> Forbidden")
    void testAssignPlanTo_NotClient() throws Exception {
        // Arrange
        String planId = UUID.randomUUID().toString();
        WorkoutPlan templatePlan = new WorkoutPlan(planId, "Template Plan", 7);
        templatePlan.setAuthorId(mockIdentityProvider.getUserId());
        workoutPlanDao.savePlan(templatePlan);

        String athleteId = UUID.randomUUID().toString();
        // Atleta non collegato al trainer...

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            controller.assignPlanTo(planId, athleteId);
        });
    }

    @Test
    @DisplayName("Trainer modifica un suo piano")
    void testUpdatePlan_Success() throws Exception {
        // Arrange
        String planId = UUID.randomUUID().toString();
        WorkoutPlan plan = new WorkoutPlan(planId, "Vecchio Titolo", 7);
        plan.setAuthorId(mockIdentityProvider.getUserId());
        workoutPlanDao.savePlan(plan);

        WorkoutPlanDTO updateBean = new WorkoutPlanDTO();
        updateBean.setPlanId(planId);
        updateBean.setName("Titolo Modificato");
        updateBean.setCycleLength(10);
        updateBean.setWorkoutSessions(List.of(
                new WorkoutSessionDTO("Giorno 1", "{}", 0)
        ));

        // Act
        controller.updatePlan(planId, updateBean);

        // Assert
        Optional<WorkoutPlan> updatedOpt = workoutPlanDao.findPlanById(planId);
        assertTrue(updatedOpt.isPresent());
        assertEquals("Titolo Modificato", updatedOpt.get().getTitle());
        assertEquals(10, updatedOpt.get().getCycleLength());
    }

    @Test
    @DisplayName("Trainer elimina un suo piano")
    void testDeletePlan_Success() throws Exception {
        // Arrange
        String planId = UUID.randomUUID().toString();
        WorkoutPlan plan = new WorkoutPlan(planId, "Da Eliminare", 7);
        plan.setAuthorId(mockIdentityProvider.getUserId());
        workoutPlanDao.savePlan(plan);

        // Act
        controller.deletePlan(planId);

        // Assert
        Optional<WorkoutPlan> deletedOpt = workoutPlanDao.findPlanById(planId);
        assertTrue(deletedOpt.isEmpty());
    }
}