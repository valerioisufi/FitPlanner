package com.example.fitplannerclient.controller.plan.editor.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class EditorHistoryManager {
    private final Deque<WorkoutPlanEditorCommand> undoStack = new ArrayDeque<>();
    private final Deque<WorkoutPlanEditorCommand> redoStack = new ArrayDeque<>();

    public void executeCommand(WorkoutPlanEditorCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            WorkoutPlanEditorCommand command = undoStack.pop();
            command.undo();

            redoStack.push(command);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            WorkoutPlanEditorCommand command = redoStack.pop();
            command.execute();

            undoStack.push(command);
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
