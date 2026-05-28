package com.example.fitplannerserver.beanvalidator;

import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.RegisterDTO;
import com.example.fitplannercommon.TokenDTO;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.util.ValidationUtils;

import java.util.regex.Pattern;

public class AuthValidator {

    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,32}$");

    private AuthValidator(){}

    private static void validatePassword(String password){
        if(!PASSWORD_REGEX.matcher(password).matches()){
            throw new WrongArgumentsException("La password deve essere lunga 8-32 caratteri e contenere almeno una lettera maiuscola, una minuscola e un numero");
        }
    }

    public static void validateLoginBean(LoginDTO bean){
        if(bean == null || bean.getEmail() == null || bean.getPassword() == null){
            throw new WrongArgumentsException("Email e password sono obbligatorie");

        }

        if(!ValidationUtils.isValidEmail(bean.getEmail())){
            throw new WrongArgumentsException("Email non valida");
        }

    }

    public static void validateRegisterBean(RegisterDTO bean){
        if(bean == null || bean.getEmail() == null || bean.getPassword() == null || bean.getProfile() == null){
            throw new WrongArgumentsException("Email, password e profilo sono obbligatori");
        }

        if(!ValidationUtils.isValidEmail(bean.getEmail())){
            throw new WrongArgumentsException("L'email non è in un formato valido");
        }

        validatePassword(bean.getPassword());

        ProfileValidator.validateProfileBean(bean.getProfile());

    }

    public static void validateRefreshTokenBean(TokenDTO bean){
        if(bean == null || bean.getRefreshToken() == null){
            throw new WrongArgumentsException("Il refresh token è obbligatorio");
        }

    }

}
