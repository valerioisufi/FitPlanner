package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.ExerciseLogBean;
import com.example.fitplannercommon.SessionLogBean;
import com.example.fitplannerserver.beanvalidator.LogValidator;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.DaoFactory;
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

    public SessionLogController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public List<SessionLogBean> getFilteredSessionLog(String athleteId, long startDate, long endDate) {
        if (!ValidationUtils.isValidUuid(athleteId)) {
            throw new WrongArgumentsException("athleteId deve essere un UUID valido");
        }
        if (startDate > endDate) {
            throw new WrongArgumentsException("startDate non può essere successivo a endDate");
        }

        if(identityProvider.getUserRole() == Account.Role.ATHLETE){
            if (!Objects.equals(identityProvider.getUserId(), athleteId)) {
                throw new UnauthorizedException("Gli atleti possono solo accedere ai propri session logs");
            }
        }

        if(identityProvider.getUserRole() == Account.Role.TRAINER){
            CoachingDao coachingDao = DaoFactory.getInstance().getCoachingDao();

            try {
                boolean isTrainerOfAthlete = coachingDao.isClientOf(identityProvider.getUserId(), athleteId);
                if (!isTrainerOfAthlete) {
                    throw new UnauthorizedException("I trainer possono accedere solo ai session logs dei propri atleti");
                }

            } catch (DaoException e) {
                throw new SystemException("Errore durante la verifica del rapporto trainer-atleta");
            }

        }


        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

        try {
            List<SessionLog> sessionLog = sessionLogDao.findLogsByAthleteIdAndDateRange(athleteId, startDate, endDate);

            List<SessionLogBean> sessionLogBeans = new ArrayList<>();
            for (SessionLog log : sessionLog) {
                sessionLogBeans.add(toBean(log));
            }
            return sessionLogBeans;

        } catch (DaoException e) {
            throw new SystemException("Errore nel recuperare i session logs");
        }
    }

    public void saveSessionLog(SessionLogBean logBean) {
        identityProvider.checkUserRole(Account.Role.ATHLETE);
        LogValidator.validateSessionLogBean(logBean);

        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

        SessionLog sessionLog = LogMapper.toEntity(identityProvider.getUserId(), logBean);

        try {
            sessionLogDao.saveSessionLog(sessionLog);

        } catch (DaoException e) {
            throw new SystemException("Errore nel salvare il session log");
        }
    }

    public ExerciseLogBean getLastRecordForExercise(String exerciseId) {
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        if (!ValidationUtils.isValidUuid(exerciseId)) {
            throw new WrongArgumentsException("exerciseId deve essere un UUID valido");
        }

        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

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
            throw new SystemException("Errore nel recuperare l'ultimo log per l'esercizio");
        }

    }


}
