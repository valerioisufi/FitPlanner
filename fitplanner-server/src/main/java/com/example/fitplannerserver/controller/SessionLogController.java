package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.SessionLogDTO;
import com.example.fitplannerserver.beanvalidator.LogValidator;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.*;
import com.example.fitplannerserver.mapper.LogMapper;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;
import com.example.fitplannerserver.security.IdentityProvider;
import com.example.fitplannerserver.util.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.fitplannerserver.mapper.LogMapper.toBean;

public class SessionLogController {
    private final IdentityProvider identityProvider;

    private final SessionLogDao sessionLogDao;
    private final CoachingDao coachingDao;

    public SessionLogController(
            IdentityProvider identityProvider,
            SessionLogDao sessionLogDao,
            CoachingDao coachingDao
    ) {
        this.identityProvider = identityProvider;

        this.sessionLogDao = sessionLogDao;
        this.coachingDao = coachingDao;
    }

    public List<SessionLogDTO> getFilteredSessionLog(String athleteId, long startDate, long endDate) {
        if (startDate > endDate) {
            throw new WrongArgumentsException("startDate non può essere successivo a endDate");
        }

        if(identityProvider.getUserRole() == Account.Role.ATHLETE && athleteId != null && !Objects.equals(identityProvider.getUserId(), athleteId)){
            throw new ForbiddenException("Gli atleti possono solo accedere ai propri session logs");
        }

        String userId = (identityProvider.getUserRole() == Account.Role.ATHLETE) ? identityProvider.getUserId() : athleteId;

        if(identityProvider.getUserRole() == Account.Role.TRAINER){
            try {
                boolean isTrainerOfAthlete = coachingDao.isClientOf(identityProvider.getUserId(), userId);
                if (!isTrainerOfAthlete) {
                    throw new ForbiddenException("I trainer possono accedere solo ai session logs dei propri atleti");
                }

            } catch (DaoException e) {
                throw new SystemException("Errore durante la verifica del rapporto trainer-atleta", e);
            }
        }

        try {
            List<SessionLog> sessionLog = sessionLogDao.findLogsByAthleteIdAndDateRange(userId, startDate, endDate);

            List<SessionLogDTO> sessionLogDTOS = new ArrayList<>();
            for (SessionLog log : sessionLog) {
                sessionLogDTOS.add(toBean(log));
            }
            return sessionLogDTOS;

        } catch (DaoException e) {
            throw new SystemException("Errore nel recuperare i session logs", e);
        }
    }

    public void saveSessionLog(SessionLogDTO logBean) {
        identityProvider.checkUserRole(Account.Role.ATHLETE);
        LogValidator.validateSessionLogBean(logBean);

        SessionLog sessionLog = LogMapper.toEntity(identityProvider.getUserId(), logBean);

        try {
            sessionLogDao.saveSessionLog(sessionLog);

        } catch (DaoException e) {
            throw new SystemException("Errore nel salvare il session log", e);
        }
    }

    public ExerciseLogDTO getLastRecordForExercise(String exerciseId) {
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        if (!ValidationUtils.isValidUuid(exerciseId)) {
            throw new WrongArgumentsException("exerciseId deve essere un UUID valido");
        }

        try {
            Optional<SessionLog> sessionLog = sessionLogDao
                    .findMostRecentSessionContainingExercise(
                            identityProvider.getUserId(),
                            exerciseId
                    );

            if (sessionLog.isPresent()) {

                for (ExerciseLog exerciseLog : sessionLog.get().getExerciseLogs()) {
                    if (exerciseLog.getExerciseId().equals(exerciseId)) {
                        return LogMapper.toBean(exerciseLog);
                    }
                }
            }

            throw new ResourceNotFoundException("Esercizio non trovato in nessun session log recente");

        } catch (DaoException e) {
            throw new SystemException("Errore nel recuperare l'ultimo log per l'esercizio", e);
        }

    }


}
