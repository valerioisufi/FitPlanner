package com.example.fitplannerclient.bean.exercise;

import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;

import java.util.ArrayList;
import java.util.List;

public class CurrentExerciseBean {
    private ExerciseDescriptionBean exerciseDescription;
    private List<ExerciseModifierBean> modifiers;

    public CurrentExerciseBean() {
        this.modifiers = new ArrayList<>();
    }

    public CurrentExerciseBean(ExerciseDescriptionBean exerciseDescription, List<ExerciseModifierBean> modifiers) {
        this.exerciseDescription = exerciseDescription;
        this.modifiers = modifiers != null ? modifiers : new ArrayList<>();
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
}
