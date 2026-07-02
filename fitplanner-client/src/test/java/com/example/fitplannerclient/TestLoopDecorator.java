package com.example.fitplannerclient;

import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;
import com.example.fitplannerclient.entity.plan.decorator.LoopDecorator;
import com.example.fitplannerclient.mock.DummyPlanNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test dell'esecuzione di un LoopDecorator
 * @author Valerio Isufi
 */
class TestLoopDecorator {

    @Test
    void TextExecuteChildRunning() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.RUNNING, 1000); // il wrappedNode è in esecuzione

        LoopDecorator loop = new LoopDecorator(child, "3");
        ExecutionContext context = new ExecutionContext();

        // Act
        ExecutionResult result = loop.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, result.getState());
        assertEquals(1000, result.getRequestedSleepMillis());
        assertEquals(1, child.getExecuteCallCount());
        assertEquals(0, child.getResetCallCount());
    }

    @Test
    void TextExecuteChildCompleted() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        child.setNextResult(PlanNodeState.COMPLETED); // il wrappedNode ha terminato

        LoopDecorator loop = new LoopDecorator(child, "2");
        ExecutionContext context = new ExecutionContext();

        // Act
        ExecutionResult result = loop.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, result.getState());
        assertEquals(2, child.getExecuteCallCount()); //
        assertEquals(1, child.getResetCallCount()); // il wrappedNode è stato resettato
    }

    @Test
    void TestExecuteTargetRoundsReached() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        LoopDecorator loop = new LoopDecorator(child, "2");
        ExecutionContext context = new ExecutionContext();

        // ROUND 1
        child.setNextResult(PlanNodeState.COMPLETED);
        loop.execute(context); // wrappedNode completa il round 1, il decorator lo resetta

        // ROUND 2
        child.setNextResult(PlanNodeState.COMPLETED);
        loop.execute(context); // completa il round 2

        // Act
        ExecutionResult finalResult = loop.execute(context);

        // Assert
        assertEquals(PlanNodeState.COMPLETED, finalResult.getState());
        
        // verifico che il wrappedNode sia stato il numero di volte richiesto
        // (2 round + 1 esecuzione intermedia con stato RUNNING)
        assertEquals(3, child.getExecuteCallCount());
    }

    @Test
    void TestResetCurrentRoundAndChild() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        LoopDecorator loop = new LoopDecorator(child, "5");
        ExecutionContext context = new ExecutionContext();

        child.setNextResult(PlanNodeState.COMPLETED);
        loop.execute(context); // resetCallCount diventa 1 internamente

        // Act
        loop.reset();

        // Assert
        assertEquals(2, child.getResetCallCount()); // 1 dal loop normale + 1 esplicito
        
        // verifico che i round interni siano stati azzerati
        for(int i=0; i<4; i++) {
            child.setNextResult(PlanNodeState.COMPLETED);
            assertEquals(PlanNodeState.RUNNING, loop.execute(context).getState());
        }
        child.setNextResult(PlanNodeState.COMPLETED);
        assertEquals(PlanNodeState.COMPLETED, loop.execute(context).getState());
    }

    @Test
    void TestExecuteChildSkipped() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        LoopDecorator loop = new LoopDecorator(child, "2");
        ExecutionContext context = new ExecutionContext();

        // Act
        child.setNextResult(PlanNodeState.SKIPPED);
        ExecutionResult finalResult = loop.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, finalResult.getState());
    }

    @Test
    void TestExecuteChildRevertWhenFirstRound() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        LoopDecorator loop = new LoopDecorator(child, "2");
        ExecutionContext context = new ExecutionContext();

        // Act
        child.setNextResult(PlanNodeState.REVERT);
        ExecutionResult finalResult = loop.execute(context);

        // Assert
        assertEquals(PlanNodeState.REVERT, finalResult.getState());
    }

    @Test
    void TestExecuteChildRevertWhenMidLoop() {
        // Arrange
        DummyPlanNode child = new DummyPlanNode();
        LoopDecorator loop = new LoopDecorator(child, "2");
        ExecutionContext context = new ExecutionContext();

        // Act
        child.setNextResult(PlanNodeState.COMPLETED);
        loop.execute(context);

        child.setNextResult(PlanNodeState.REVERT);
        ExecutionResult finalResult = loop.execute(context);

        // Assert
        assertEquals(PlanNodeState.RUNNING, finalResult.getState());
    }
}