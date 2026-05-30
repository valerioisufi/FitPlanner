package com.example.fitplannerclient.controller.plan.command;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand implements WorkoutPlanEditorCommand {

    private final List<WorkoutPlanEditorCommand> commands;

    public CompositeCommand() {
        this.commands = new ArrayList<>();
    }

    public void addCommand(WorkoutPlanEditorCommand command) {
        commands.add(command);
    }

    @Override
    public void execute() {
        for (WorkoutPlanEditorCommand command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }
}
