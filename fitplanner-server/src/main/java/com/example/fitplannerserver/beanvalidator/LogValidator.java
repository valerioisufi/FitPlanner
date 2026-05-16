package com.example.fitplannerserver.beanvalidator;

import com.example.fitplannercommon.ExerciseLogBean;
import com.example.fitplannercommon.SessionLogBean;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.util.ValidationUtils;

public class LogValidator {

    private LogValidator(){}

    public static void validateSessionLogBean(SessionLogBean bean) {
        if (bean == null) {
            throw new WrongArgumentsException("session log non può essere nullo");
        }
        if (bean.getNotes() == null || bean.getStatus() == null || bean.getExerciseLogs() == null) {
            throw new WrongArgumentsException("I campi notes, sessionStatus e exerciseLogs non possono essere nulli");
        }

        if (ValidationUtils.isLengthAtMost(bean.getNotes(), 1000)) {
            throw new WrongArgumentsException("Il campo notes non può superare i 1000 caratteri");
        }

        if (bean.getDate() <= 0) {
            throw new WrongArgumentsException("Il campo date non può essere negativo");
        }

        for (ExerciseLogBean exLog : bean.getExerciseLogs()) {
            validateExerciseLogBean(exLog);
        }
    }

    private static void validateExerciseLogBean(ExerciseLogBean bean){
        if(bean == null){
            throw new WrongArgumentsException("exercise log non può essere nullo");
        }
        if(bean.getName() == null || bean.getExerciseId() == null || bean.getSets() == null || bean.getNotes() == null){
            throw new WrongArgumentsException("I campi name, exerciseId, sets e notes non possono essere nulli");
        }

        if (ValidationUtils.isLengthBetween(bean.getName(), 1,100)) {
            throw new WrongArgumentsException("Il campo name non può essere vuoto o superare i 100 caratteri");
        }

        if (ValidationUtils.isValidUuid(bean.getExerciseId())) {
            throw new WrongArgumentsException("exerciseId deve essere un UUID valido");
        }

        if(ValidationUtils.isLengthAtMost(bean.getNotes(), 250)){
            throw new WrongArgumentsException("Il campo notes non può superare i 250 caratteri");
        }
    }
}
