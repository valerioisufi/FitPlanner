package com.example.fitplannerclient;

import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test dell'esecuzione di un ExerciseNode
 * @author Valerio Isufi
 */

public class TestExerciseNode {

    @Test
    void testExecuteRunning() {
        // Arrange
        ExerciseNode node = new ExerciseNode();
        ExecutionContext context = new ExecutionContext();

        // Act
        ExecutionResult result = node.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, result.getState());
        assertEquals(0, result.getRequestedSleepMillis());

    }

    @Test
    void testExecuteCompleted() {
        // Arrange
        ExerciseNode node = new ExerciseNode();
        ExecutionContext context = new ExecutionContext();
        node.execute(context); // inizia l'esercizio

        context.injectSignal(ControlSignal.DONE); // l'esercizio è stato contrassegnato come completato

        // Act
        ExecutionResult result = node.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(0, result.getRequestedSleepMillis());

    }

    @Test
    void testExecuteSkipped() {
        // Arrange
        ExerciseNode node = new ExerciseNode();
        ExecutionContext context = new ExecutionContext();
        node.execute(context); // inizia l'esercizio

        context.injectSignal(ControlSignal.SKIP_NEXT); // l'utente vuole saltare l'esercizio

        // Act
        ExecutionResult result = node.execute(context);

        // Assert
        assertEquals(PlanNodeState.SKIPPED, result.getState());
        assertEquals(0, result.getRequestedSleepMillis());
        assertEquals(PlanNodeState.SKIPPED, node.getState()); // l'esercizio deve tornare allo stato iniziale
    }

    @Test
    void testExecuteRevert() {
        // Arrange
        ExerciseNode node = new ExerciseNode();
        ExecutionContext context = new ExecutionContext();
        node.execute(context); // inizia l'esercizio

        context.injectSignal(ControlSignal.SKIP_PREVIOUS); // l'utente vuole tornare all'esercizio precedente

        // Act
        ExecutionResult result = node.execute(context);

        // Assert
        assertEquals(PlanNodeState.REVERT, result.getState());
        assertEquals(0, result.getRequestedSleepMillis());
        assertEquals(PlanNodeState.IDLE, node.getState()); // l'esercizio deve tornare allo stato iniziale
    }

    @Test
    void testReset() {
        // Arrange
        ExerciseNode node = new ExerciseNode();
        ExecutionContext context = new ExecutionContext();

        // inizia e completa l'esercizio
        node.execute(context);
        context.injectSignal(ControlSignal.DONE);
        node.execute(context);

        // Act
        node.reset();

        // Assert
        assertEquals(PlanNodeState.IDLE, node.getState());
    }
}
