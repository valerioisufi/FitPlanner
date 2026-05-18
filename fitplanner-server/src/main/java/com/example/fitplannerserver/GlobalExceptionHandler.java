package com.example.fitplannerserver;

import com.example.fitplannercommon.ErrorResponseBean;
import com.example.fitplannerserver.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 401 - Non autorizzato ad accedere alla risorsa o a eseguire l'operazione
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseBean> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponseBean error = new ErrorResponseBean(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 401 - Credenziali non valide
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseBean> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponseBean error = new ErrorResponseBean(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 404 - Risorsa non trovata (plan, user, ...)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseBean> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponseBean error = new ErrorResponseBean(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 400 - Input della richiesta non valido
    @ExceptionHandler(WrongArgumentsException.class)
    public ResponseEntity<ErrorResponseBean> handleWrongArgumentsException(WrongArgumentsException ex) {
        ErrorResponseBean error = new ErrorResponseBean(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 500 - Errore generico del server legato alla logica di business
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorResponseBean> handleSystemException(SystemException ex) {
        ErrorResponseBean error = new ErrorResponseBean(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 500 - Fallback per ogni eccezione non gestita
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseBean> handleGenericException(Exception ex) {
        ErrorResponseBean error = new ErrorResponseBean(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Errore generico del server"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}