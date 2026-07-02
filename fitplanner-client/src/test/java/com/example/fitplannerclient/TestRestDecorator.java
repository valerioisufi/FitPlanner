package com.example.fitplannerclient;

import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;
import com.example.fitplannerclient.entity.plan.decorator.RestDecorator;
import com.example.fitplannerclient.mock.DummyPlanNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test dell'esecuzione di un RestDecorator
 * @author Valerio Isufi
 */
class TestRestDecorator {

    @Test
    void TestExecuteChildRunning() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.RUNNING, 1000);

        RestDecorator rest = new RestDecorator(child, "30000");

        ExecutionContext context = new ExecutionContext();
        context.setTickDelta(100);

        // Act
        ExecutionResult result = rest.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, result.getState());
        assertEquals(1000, result.getRequestedSleepMillis());
        assertEquals(1, child.getExecuteCallCount());
    }

    @Test
    void TestExecuteChildCompleted() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);

        RestDecorator rest = new RestDecorator(child, "10000");

        ExecutionContext context = new ExecutionContext();
        context.setTickDelta(2000);

        // Act
        // quando il child ritorna COMPLETED, il RestDecorator passa in WAITING
        ExecutionResult result = rest.execute(context);

        // Assert
        assertEquals(PlanNodeState.WAITING, result.getState());
        assertEquals(10000, result.getRequestedSleepMillis()); 
        assertEquals(2000, context.getTickDelta()); // il tickDelta non viene consumato
        assertEquals(1, child.getExecuteCallCount());
    }

    @Test
    void TestExecuteZeroRestDuration() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);

        RestDecorator rest = new RestDecorator(child, "0");
        ExecutionContext context = new ExecutionContext();

        // Act
        ExecutionResult result = rest.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(1, child.getExecuteCallCount());
    }

    @Test
    void TestExecuteWaitingConsumesDelta() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        RestDecorator rest = new RestDecorator(child, "5000");
        ExecutionContext context = new ExecutionContext();

        child.setNextResult(PlanNodeState.COMPLETED);
        context.setTickDelta(1000);
        rest.execute(context); // WAITING

        // Act
        context.setTickDelta(1500);
        ExecutionResult result = rest.execute(context);

        // Assert
        assertEquals(PlanNodeState.WAITING, result.getState());
        assertEquals(3500, result.getRequestedSleepMillis()); // 5000 - 1500
        assertEquals(0, context.getTickDelta()); // delta consumato
    }

    @Test
    void TestExecuteWaitingCompletesWhenDeltaIsEnough() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        RestDecorator rest = new RestDecorator(child, "2000");
        ExecutionContext context = new ExecutionContext();

        child.setNextResult(PlanNodeState.COMPLETED);
        context.setTickDelta(0);
        rest.execute(context); // WAITING

        // Act
        context.setTickDelta(5000);
        ExecutionResult result = rest.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(3000, context.getTickDelta()); // 5000 - 2000
    }

    @Test
    void TestExecuteWaitingSkipNextSignal() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        RestDecorator rest = new RestDecorator(child, "5000");
        ExecutionContext context = new ExecutionContext();
        
        child.setNextResult(PlanNodeState.COMPLETED);
        context.setTickDelta(0);
        rest.execute(context); // WAITING

        // Act
        context.injectSignal(ControlSignal.SKIP_NEXT);
        context.setTickDelta(1000);
        ExecutionResult result = rest.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(ControlSignal.NONE, context.getCurrentSignal()); // segnale consumato
        assertEquals(0, context.getTickDelta()); // tempo consumato
    }

    @Test
    void TestExecuteWaitingSkipPreviousSignal() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        RestDecorator rest = new RestDecorator(child, "5000");
        ExecutionContext context = new ExecutionContext();
        
        child.setNextResult(PlanNodeState.COMPLETED);
        context.setTickDelta(0);
        rest.execute(context); // WAITING

        // Act
        context.injectSignal(ControlSignal.SKIP_PREVIOUS);
        ExecutionResult result = rest.execute(context);

        // Assert
        assertEquals(PlanNodeState.REVERT, result.getState());
        assertEquals(ControlSignal.NONE, context.getCurrentSignal());
        assertEquals(1, child.getResetCallCount()); 
    }
}
