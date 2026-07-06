package com.example.fitplannerclient.bean.exercise;

import com.example.fitplannerclient.bean.log.ExerciseLogBean;
import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.entity.log.ExerciseLog;

import java.util.ArrayList;
import java.util.List;

public class CurrentExerciseBean {
    private ExerciseDescriptionBean exerciseDescription;
    private List<ExerciseModifierBean> modifiers;

    private String breadcrumb;
    private ExerciseLogBean lastWeightLog;

    public CurrentExerciseBean() {
        this.modifiers = new ArrayList<>();
    }

    public CurrentExerciseBean(ExerciseDescriptionBean exerciseDescription, List<ExerciseModifierBean> modifiers, String breadcrumb, ExerciseLogBean lastWeightLog) {
        this.exerciseDescription = exerciseDescription;
        this.modifiers = modifiers != null ? modifiers : new ArrayList<>();

        this.breadcrumb = breadcrumb;
        this.lastWeightLog = lastWeightLog;
    }

    public ExerciseDescriptionBean getExerciseDescription() {
        return exerciseDescription;
    }

    public void setExerciseDescription(ExerciseDescriptionBean exerciseDescription) {
        this.exerciseDescription = exerciseDescription;
    }

    public List<ExerciseModifierBean> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ExerciseModifierBean> modifiers) {
        this.modifiers = modifiers;
    }

    public String getBreadcrumb() {
        return breadcrumb;
    }

    public void setBreadcrumb(String breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public ExerciseLogBean getLastWeightLog() {
        return lastWeightLog;
    }

    public void setLastWeightLog(ExerciseLogBean lastWeightLog) {
        this.lastWeightLog = lastWeightLog;
    }
}
