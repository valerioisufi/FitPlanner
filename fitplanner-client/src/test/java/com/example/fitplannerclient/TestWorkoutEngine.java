package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.plan.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.entity.plan.context.WorkoutStatus;
import com.example.fitplannerclient.mock.DummyPlanNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del motore di esecuzione WorkoutEngine
 * @author Valerio Isufi
 */
public class TestWorkoutEngine {

    @Test
    void testInitialState() {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Assert
        assertEquals(WorkoutStatus.STOPPED, engine.getState().getStatus());
    }

    @Test
    void testPlayTransition() throws InterruptedException {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Act
        engine.play();

        // aspetto un istante per permettere al Virtual Thread di avviarsi
        Thread.sleep(50);

        // Assert
        assertEquals(WorkoutStatus.PLAYING, engine.getState().getStatus());
        assertTrue(rootNode.getExecuteCallCount() > 0);

        // Cleanup
        engine.stop();
    }

    @Test
    void testPauseTransition() throws InterruptedException {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Act
        engine.play();
        Thread.sleep(20);
        engine.pause();

        // Assert
        assertEquals(WorkoutStatus.PAUSED, engine.getState().getStatus());

        // Cleanup
        engine.stop();
    }

    @Test
    void testStopTransition() throws InterruptedException {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Act
        engine.play();
        Thread.sleep(20);
        engine.stop();

        // aspetto che il thread recepisca l'interruzione
        Thread.sleep(50);

        // Assert
        assertEquals(WorkoutStatus.STOPPED, engine.getState().getStatus());
        // lo stop dovrebbe chiamare il reset dell'albero (1 volta alla creazione dell'engine, 1 volta allo stop)
        assertEquals(2, rootNode.getResetCallCount());
    }

    @Test
    void testSkipNextInPause() {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        engine.play();
        engine.pause();

        int executeCallsBeforeSkip = rootNode.getExecuteCallCount();

        // Act
        engine.skipNext();

        // Assert
        assertEquals(WorkoutStatus.PAUSED, engine.getState().getStatus());
        // lo skip durante PauseState forza una chiamata ad execute() per processare il segnale
        assertEquals(executeCallsBeforeSkip + 1, rootNode.getExecuteCallCount());
    }

    @Test
    void testDoneInPause() {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        engine.play();
        engine.pause();

        int executeCallsBeforeDone = rootNode.getExecuteCallCount();

        // Act
        engine.done();

        // Assert
        assertEquals(WorkoutStatus.PAUSED, engine.getState().getStatus());
        assertEquals(executeCallsBeforeDone + 1, rootNode.getExecuteCallCount());
    }
}
