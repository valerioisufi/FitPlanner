package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.entity.plan.execution.WorkoutStatus;
import com.example.fitplannerclient.mock.DummyPlanNode;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del motore di esecuzione WorkoutEngine
 * @author Valerio Isufi
 */
class TestWorkoutEngine {

    @Test
    void testInitialState() {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Assert
        assertEquals(WorkoutStatus.STOPPED, engine.getState().getStatus());
    }

    @Test
    void testPlayTransition() {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Act
        engine.play();

        // Assert
        await().atMost(200, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            assertEquals(WorkoutStatus.PLAYING, engine.getState().getStatus());
            assertTrue(rootNode.getExecuteCallCount() > 0);
        });

        // Cleanup
        engine.stop();
    }

    @Test
    void testPauseTransition() {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Act
        engine.play();
        
        await().atMost(200, TimeUnit.MILLISECONDS).until(() ->
                engine.getState().getStatus() == WorkoutStatus.PLAYING
        );
        
        engine.pause();

        // Assert
        assertEquals(WorkoutStatus.PAUSED, engine.getState().getStatus());

        // Cleanup
        engine.stop();
    }

    @Test
    void testStopTransition() {
        // Arrange
        DummyPlanNode rootNode = new DummyPlanNode();
        WorkoutEngineImpl engine = new WorkoutEngineImpl(rootNode);

        // Act
        engine.play();
        
        await().atMost(200, TimeUnit.MILLISECONDS).until(() ->
                engine.getState().getStatus() == WorkoutStatus.PLAYING
        );
        
        engine.stop();

        // Assert
        await().atMost(200, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            assertEquals(WorkoutStatus.STOPPED, engine.getState().getStatus());
            assertEquals(2, rootNode.getResetCallCount());
        });
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
