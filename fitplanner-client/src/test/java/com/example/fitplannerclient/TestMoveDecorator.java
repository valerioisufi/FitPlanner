package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.plan.editor.WorkoutPlanBadgeEditor;
import com.example.fitplannerclient.controller.plan.editor.command.EditorHistoryManager;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.decorator.LoopDecorator;
import com.example.fitplannerclient.entity.plan.decorator.RestDecorator;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test dello spostamento di un FlowDecorator da un nodo a un altro nell'editor
 * @author Valerio Isufi
 */
class TestMoveDecorator {

    private final WorkoutPlanBadgeEditor badgeEditor =
            new WorkoutPlanBadgeEditor(new EditorHistoryManager(), new WorkoutPlanSubject());

    private WorkoutPlan buildPlan(Block root) {
        WorkoutPlan plan = new WorkoutPlan("Piano di test");
        plan.addSession(new WorkoutSession("Sessione", 1, root));
        return plan;
    }

    @Test
    void TestMoveToUndecoratedNode() {
        // Arrange: root -> [Rest(A), B]
        Block root = new Block("Root");
        ExerciseNode exerciseA = new ExerciseNode("ex-a");
        ExerciseNode exerciseB = new ExerciseNode("ex-b");
        RestDecorator rest = new RestDecorator(exerciseA, "60");
        root.addNode(rest);
        root.addNode(exerciseB);
        WorkoutPlan plan = buildPlan(root);

        // Act: sposto Rest da A a B
        badgeEditor.moveDecorator(plan, rest.getId(), exerciseB.getId(), 0);

        // Assert: root -> [A, Rest(B)]
        assertSame(exerciseA, root.getNodeAt(0));
        PlanNode moved = root.getNodeAt(1);
        assertInstanceOf(RestDecorator.class, moved);
        assertSame(exerciseB, ((RestDecorator) moved).getWrappedNode());
    }

    @Test
    void TestMoveOutsideExistingChain() {
        // Arrange: root -> [Rest(A), Loop(B)]
        Block root = new Block("Root");
        ExerciseNode exerciseA = new ExerciseNode("ex-a");
        ExerciseNode exerciseB = new ExerciseNode("ex-b");
        RestDecorator rest = new RestDecorator(exerciseA, "60");
        LoopDecorator loop = new LoopDecorator(exerciseB, "3");
        root.addNode(rest);
        root.addNode(loop);
        WorkoutPlan plan = buildPlan(root);

        // Act: sposto Rest su B con indice 0 (più esterno della catena)
        badgeEditor.moveDecorator(plan, rest.getId(), exerciseB.getId(), 0);

        // Assert: root -> [A, Rest(Loop(B))]
        assertSame(exerciseA, root.getNodeAt(0));
        PlanNode outer = root.getNodeAt(1);
        assertInstanceOf(RestDecorator.class, outer);
        assertSame(loop, ((RestDecorator) outer).getWrappedNode());
        assertSame(exerciseB, loop.getWrappedNode());
    }

    @Test
    void TestMoveInsideExistingChain() {
        // Arrange: root -> [Rest(A), Loop(B)]
        Block root = new Block("Root");
        ExerciseNode exerciseA = new ExerciseNode("ex-a");
        ExerciseNode exerciseB = new ExerciseNode("ex-b");
        RestDecorator rest = new RestDecorator(exerciseA, "60");
        LoopDecorator loop = new LoopDecorator(exerciseB, "3");
        root.addNode(rest);
        root.addNode(loop);
        WorkoutPlan plan = buildPlan(root);

        // Act: sposto Rest su B con indice 1 (dentro il Loop)
        badgeEditor.moveDecorator(plan, rest.getId(), exerciseB.getId(), 1);

        // Assert: root -> [A, Loop(Rest(B))]
        assertSame(exerciseA, root.getNodeAt(0));
        assertSame(loop, root.getNodeAt(1));
        PlanNode inner = loop.getWrappedNode();
        assertInstanceOf(RestDecorator.class, inner);
        assertSame(exerciseB, ((RestDecorator) inner).getWrappedNode());
    }
}
