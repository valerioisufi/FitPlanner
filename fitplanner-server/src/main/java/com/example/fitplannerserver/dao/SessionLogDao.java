package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.SessionLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionLogDao {
    List<SessionLog> findByDate(String email, LocalDateTime startDate, LocalDateTime endDate) throws DaoException;

    void save(String email, SessionLog sessionLog) throws DaoException;
}
