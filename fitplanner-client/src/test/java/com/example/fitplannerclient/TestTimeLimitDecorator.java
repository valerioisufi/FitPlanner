package com.example.fitplannerclient;

import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;
import com.example.fitplannerclient.entity.plan.decorator.TimeLimitDecorator;
import com.example.fitplannerclient.mock.DummyPlanNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test dell'esecuzione di un TimeLimitDecorator.
 * @author Valerio Isufi
 */
public class TestTimeLimitDecorator {

    @Test
    void TestExecuteChildRunning() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.RUNNING);

        TimeLimitDecorator timeLimit = new TimeLimitDecorator(child, "60000"); // 60s
        ExecutionContext context = new ExecutionContext();
        context.setTickDelta(1000); // passa 1 secondo

        // Act
        ExecutionResult result = timeLimit.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, result.getState());
        assertEquals(1, child.getExecuteCallCount());
        assertEquals(59000, result.getRequestedSleepMillis());
        assertEquals(1000, context.getTickDelta());
    }

    @Test
    void TestExecuteTimerExpires() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.RUNNING);
        
        TimeLimitDecorator timeLimit = new TimeLimitDecorator(child, "500");
        ExecutionContext context = new ExecutionContext();

        context.setTickDelta(1000); 

        // Act
        ExecutionResult result = timeLimit.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(500, context.getTickDelta());
        assertEquals(0, child.getExecuteCallCount());
    }

    @Test
    void TestExecuteChildCompletesEarly() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);
        
        TimeLimitDecorator timeLimit = new TimeLimitDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();
        context.setTickDelta(5000);

        // Act
        ExecutionResult result = timeLimit.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(5000, context.getTickDelta()); // il figlio stabilisce il consumo del tickDelta
    }

    @Test
    void TestExecuteChildSkipped() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.SKIPPED);
        
        TimeLimitDecorator timeLimit = new TimeLimitDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();

        // Act
        ExecutionResult result = timeLimit.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
    }

    @Test
    void TestExecuteChildReverts() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        TimeLimitDecorator timeLimit = new TimeLimitDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();

        child.setNextResult(PlanNodeState.RUNNING);
        context.setTickDelta(5000);
        timeLimit.execute(context); 

        // il figlio lancia REVERT
        child.setNextResult(PlanNodeState.REVERT);
        context.setTickDelta(1000);
        ExecutionResult result = timeLimit.execute(context);

        // Assert
        assertEquals(PlanNodeState.REVERT, result.getState());
        assertEquals(1, child.getResetCallCount());
        
        // verifico che il timer interno sia stato resettato
        child.setNextResult(PlanNodeState.RUNNING);
        context.setTickDelta(60000);
        ExecutionResult nextResult = timeLimit.execute(context);

        assertEquals(PlanNodeState.COMPLETED, nextResult.getState());
    }
}
