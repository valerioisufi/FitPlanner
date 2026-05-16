package com.example.fitplannerserver.beanvalidator;

import com.example.fitplannercommon.ExerciseDescriptionBean;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.util.ValidationUtils;

public class PlanValidator {

    private PlanValidator(){}

    public static void validateExerciseDescriptionBean(ExerciseDescriptionBean bean){
        if(bean == null || bean.getName() == null || bean.getExecution() == null || bean.getMuscleGroups() == null){
            throw new WrongArgumentsException("ExerciseDescriptionBean e i suoi campi non possono essere nulli");
        }

        if(!ValidationUtils.isLengthBetween(bean.getName(), 1, 50)){
            throw new WrongArgumentsException("Il nome dell'esercizio deve essere compreso tra 1 e 50 caratteri");
        }

        if(!ValidationUtils.isLengthAtMost(bean.getExecution(), 1000)){
            throw new WrongArgumentsException("La descrizione dell'esecuzione deve essere compresa tra 1 e 1000 caratteri");
        }

        for(String mg: bean.getMuscleGroups()){
            if(mg == null || mg.isBlank()){
                throw new WrongArgumentsException("I nomi dei gruppi muscolari non possono essere nulli o vuoti");
            }
            if(!ValidationUtils.isLengthAtMost(mg, 50)){
                throw new WrongArgumentsException("I nomi dei gruppi muscolari devono essere al massimo di 50 caratteri");
            }
        }

    }
}
