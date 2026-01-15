package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.ExerciseBean;
import com.example.fitplannercommon.WorkoutPlanBean;
import com.example.fitplannercommon.WorkoutSessionBean;
import com.example.fitplannerserver.model.Exercise;
import com.example.fitplannerserver.model.WorkoutPlan;
import com.example.fitplannerserver.model.WorkoutSession;
import com.example.fitplannerserver.security.SessionProvider;

import java.util.List;

public class WorkoutPlanController {
    private final SessionProvider sessionProvider;

    public WorkoutPlanController(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public void retrieveWorkoutPlan() {



    }
    public void updateWorkoutPlan(WorkoutPlanBean planBean) {
        if (planBean != null) {

        }

//        WorkoutPlan plan = new WorkoutPlan(); //da modificare con la funzione della dao

    }

    private Exercise convertExercise(ExerciseBean bean) {
        return new Exercise(
                bean.getRep(),
                bean.getSet(),
                bean.getRest(),
                bean.getTechnique(),
                bean.getNotes()
        );
    }
    private WorkoutSession convertWorkoutSession(WorkoutSessionBean bean) {
        List<Exercise> exercises = bean.getExercises().stream().map(this::convertExercise).toList();

        return new WorkoutSession(
                bean.getName(),
                exercises,
                bean.getDay(),
                bean.getState()
        );
    }

    private WorkoutPlan convertWorkoutPlan(WorkoutPlanBean bean){
        WorkoutPlan plan = new WorkoutPlan(bean.getName());

        for (WorkoutSessionBean sessionBean : bean.getWorkoutSessions()) {
            WorkoutSession session = convertWorkoutSession(sessionBean);
            plan.addSession(session);
        }
        return plan;
    }

//    private WorkoutPlan assignWorkoutPlan(WorkoutPlanBean bean){
//        return new WorkoutPlan();
//    }
}
