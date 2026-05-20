package com.example.fitplannerserver.beanvalidator;

import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.util.ValidationUtils;

public class ProfileValidator {

    private ProfileValidator(){}

    public static void validateProfileBean(ProfileDTO bean){
        if (bean == null) {
            throw new WrongArgumentsException("Il profilo non può essere nullo");
        }

        if(bean.getFirstName() == null || bean.getLastName() == null ||
                bean.getPhoneNumber() == null ||
                bean.getContactEmail() == null) {
            throw new WrongArgumentsException("Non sono ammessi valori nulli per i campi firstName, lastName, username, phoneNumber e contactEmail");
        }

        if (!ValidationUtils.isLengthBetween(bean.getFirstName(), 1,50) && !ValidationUtils.isValidName(bean.getFirstName())) {
            throw new WrongArgumentsException("Il nome non può essere vuoto o superare 50 caratteri. Deve contenere caratteri validi");
        }

        if (!ValidationUtils.isLengthBetween(bean.getLastName(), 1,50) && !ValidationUtils.isValidName(bean.getLastName())) {
            throw new WrongArgumentsException("Il cognome non può essere vuoto o superare 50 caratteri. Deve contenere caratteri validi");
        }

        if (!ValidationUtils.isValidEmail(bean.getContactEmail())) {
            throw new WrongArgumentsException("L'email non è in un formato valido");
        }

        String phone = bean.getPhoneNumber(); // il numero di telefono è opzionale
        if (!phone.trim().isEmpty() && !ValidationUtils.isValidPhoneNumber(phone)) {
            throw new WrongArgumentsException("Il numero di telefono non è in un formato valido");
        }
    }
}
