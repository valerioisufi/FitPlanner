package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.SessionLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionLogDao {
    List<SessionLog> findByDate(String email, LocalDateTime startDate, LocalDateTime endDate);

    void save(String email, SessionLog sessionLog);
}
