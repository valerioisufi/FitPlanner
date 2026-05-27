package com.example.fitplannerclient.controller.plan.command;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;

public class RemoveSessionCommand implements WorkoutPlanEditorCommand {

    private WorkoutPlan plan;
    private int sessionDay;
    private WorkoutSession session;

    public RemoveSessionCommand(WorkoutPlan plan, int sessionDay) {
        this.plan = plan;
        this.sessionDay = sessionDay;
    }

    @Override
    public void execute() {
        session = plan.removeSession(sessionDay);
    }

    @Override
    public void undo() {
        plan.addSession(session);
    }
}
