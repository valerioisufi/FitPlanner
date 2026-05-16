package com.example.fitplannerserver.beanvalidator;

import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.util.ValidationUtils;

public class ProfileValidator {

    private ProfileValidator(){}

    public static void validateProfileBean(ProfileBean bean){
        if (bean == null) {
            throw new WrongArgumentsException("Il profilo non può essere nullo");
        }

        if(bean.getFirstName() == null || bean.getLastName() == null ||
                bean.getUsername() == null || bean.getPhoneNumber() == null ||
                bean.getContactEmail() == null) {
            throw new WrongArgumentsException("Non sono ammessi valori nulli per i campi firstName, lastName, username, phoneNumber e contactEmail");
        }

        if (!ValidationUtils.isLengthBetween(bean.getUsername(), 3, 30)) {
            throw new WrongArgumentsException("Lo username deve essere compreso tra 3 e 30 caratteri");
        }

        if (!ValidationUtils.isLengthBetween(bean.getFirstName(), 1,50)) {
            throw new WrongArgumentsException("Il nome non può superare 50 caratteri");
        }

        if (!ValidationUtils.isLengthBetween(bean.getLastName(), 1,50)) {
            throw new WrongArgumentsException("Il cognome non può superare 50 caratteri");
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
