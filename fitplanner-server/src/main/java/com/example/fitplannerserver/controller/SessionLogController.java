package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.SessionLogBean;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.UpdateFailureException;
import com.example.fitplannerserver.model.SessionLog;
import com.example.fitplannerserver.security.IdentityProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionLogController {
    private final IdentityProvider identityProvider;

    public SessionLogController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public List<SessionLogBean> getSessionFilteredSessionLog(LocalDateTime startDate, LocalDateTime endDate) throws UpdateFailureException {
        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

        try {
            List<SessionLog> sessionLog = sessionLogDao.findByDate(identityProvider.getEmail(), startDate, endDate);

            List<SessionLogBean> sessionLogBeans = new ArrayList<>();
            for (SessionLog log : sessionLog) {
                sessionLogBeans.add(convertSessionLogEntity(log));
            }
            return sessionLogBeans;

        } catch (DaoException e) {
            throw new UpdateFailureException("Unable to retrieve session logs at this time");
        }
    }

    private SessionLogBean convertSessionLogEntity(SessionLog sessionLog){
        SessionLogBean.SessionStatus sessionStatus = switch (sessionLog.getStatus()){
            case COMPLETED -> SessionLogBean.SessionStatus.COMPLETED;
            case INTERRUPTED -> SessionLogBean.SessionStatus.INTERRUPTED;
            case SKIPPED -> SessionLogBean.SessionStatus.SKIPPED;
        };

        return new SessionLogBean(
                sessionLog.getNotes(),
                sessionStatus,
                sessionLog.getDate()
        );
    }

    public void updateSessionLog(SessionLogBean sessionLogBean, LocalDateTime date) throws UpdateFailureException {
        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

        try {
            List<SessionLog> sessionLogs = sessionLogDao.findByDate(identityProvider.getEmail(), date, date);

            if(sessionLogs.isEmpty()){
                throw new UpdateFailureException("Session log not found for the specified date");
            }

            SessionLog sessionLog = sessionLogs.getFirst();

            SessionLog.SessionStatus sessionStatus = switch (sessionLogBean.getStatus()){
                case COMPLETED -> SessionLog.SessionStatus.COMPLETED;
                case INTERRUPTED -> SessionLog.SessionStatus.INTERRUPTED;
                case SKIPPED -> SessionLog.SessionStatus.SKIPPED;
            };

            sessionLog.setNotes(sessionLogBean.getNotes());
            sessionLog.setSessionStatus(sessionStatus);
            sessionLog.setDate(sessionLogBean.getDate());

            sessionLogDao.save(identityProvider.getEmail(), sessionLog);

        } catch (DaoException e) {
            throw new UpdateFailureException("Failed to update session log due to a system error");
        }
    }
}
