package com.example.fitplannerclient;

import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;
import com.example.fitplannerclient.entity.plan.decorator.IntervalDecorator;
import com.example.fitplannerclient.mock.DummyPlanNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test dell'esecuzione di un IntervalDecorator (protocolli EMOM).
 * @author Valerio Isufi
 */
class TestIntervalDecorator {

    @Test
    void TestExecuteChildRunning() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.RUNNING);

        IntervalDecorator interval = new IntervalDecorator(child, "60000"); // 60s
        ExecutionContext context = new ExecutionContext();
        context.setTickDelta(1000); // passa 1 secondo

        // Act
        ExecutionResult result = interval.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, result.getState());
        assertEquals(1, child.getExecuteCallCount());
        assertEquals(59000, result.getRequestedSleepMillis());
        assertEquals(1000, context.getTickDelta());
    }

    @Test
    void TestExecuteChildFinishesEarly() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);
        
        IntervalDecorator interval = new IntervalDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();
        context.setTickDelta(10000); // l'esercizio ha impiegato 10s per finire

        // Act
        ExecutionResult result = interval.execute(context);

        // Assert
        assertEquals(PlanNodeState.WAITING, result.getState());
        assertEquals(50000, result.getRequestedSleepMillis()); // 50 secondi di riposo
        assertEquals(10000, context.getTickDelta()); // tickDelta non consumato
    }

    @Test
    void TestExecuteIntervalTimerExpires() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.RUNNING);

        IntervalDecorator interval = new IntervalDecorator(child, "500");
        ExecutionContext context = new ExecutionContext();

        context.setTickDelta(1000); 

        // Act
        ExecutionResult result = interval.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(500, context.getTickDelta()); // avanza 500ms
        assertEquals(0, child.getExecuteCallCount()); // il figlio non viene eseguito
    }

    @Test
    void TestExecuteWaitingPhaseConsumesDelta() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);
        
        IntervalDecorator interval = new IntervalDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();
        
        // finisce l'esercizio
        context.setTickDelta(10000);
        interval.execute(context); // passa in WAITING con 50s rimanenti

        // Act
        context.setTickDelta(20000); // passano altri 20s
        ExecutionResult result = interval.execute(context);

        // Assert
        assertEquals(PlanNodeState.WAITING, result.getState());
        assertEquals(30000, result.getRequestedSleepMillis()); // 50s - 20s = 30s
        assertEquals(0, context.getTickDelta()); // il tickDelta viene consumato
    }

    @Test
    void TestExecuteWaitingPhaseTimerExpires() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);
        
        IntervalDecorator interval = new IntervalDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();
        
        context.setTickDelta(10000);
        interval.execute(context); // WAITING con 50s rimanenti

        // Act
        context.setTickDelta(60000); // passano 60s
        ExecutionResult result = interval.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(10000, context.getTickDelta()); 
    }

    @Test
    void TestExecuteWaitingPhaseSkipNext() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);
        
        IntervalDecorator interval = new IntervalDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();
        
        context.setTickDelta(0);
        interval.execute(context); // WAITING

        // Act
        context.injectSignal(ControlSignal.SKIP_NEXT);
        context.setTickDelta(1234);
        ExecutionResult result = interval.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, result.getState());
        assertEquals(0, context.getTickDelta());
    }

    @Test
    void TestExecuteWaitingPhaseSkipPrevious() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED);
        
        IntervalDecorator interval = new IntervalDecorator(child, "60000");
        ExecutionContext context = new ExecutionContext();
        
        context.setTickDelta(0);
        interval.execute(context); // WAITING

        // Act
        context.injectSignal(ControlSignal.SKIP_PREVIOUS);
        context.setTickDelta(1234);
        ExecutionResult result = interval.execute(context);

        // Assert
        assertEquals(PlanNodeState.REVERT, result.getState());
        assertEquals(1, child.getResetCallCount());
        assertEquals(0, context.getTickDelta());
    }
}
