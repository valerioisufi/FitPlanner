package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.model.log.SessionLog;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemorySessionLogDao implements SessionLogDao{
    private static class Wrapper {
        public static final InMemorySessionLogDao INSTANCE = new InMemorySessionLogDao();
    }

    public static InMemorySessionLogDao getInstance() {
        return InMemorySessionLogDao.Wrapper.INSTANCE;
    }

    private final Map<String, List<SessionLog>> sessionLogs = new ConcurrentHashMap<>();

    @Override
    public List<SessionLog> findByDate(String email, LocalDateTime startDate, LocalDateTime endDate) {
        List<SessionLog> logs = sessionLogs.get(email);

        if (logs == null) return Collections.emptyList();

        return logs.stream().filter(log -> {
            LocalDateTime logDate = log.getDate();
            return (logDate.isEqual(startDate) || logDate.isAfter(startDate))
                    && (logDate.isEqual(endDate) || logDate.isBefore(endDate));
        }).toList();
    }

    @Override
    public void save(String email, SessionLog sessionLog) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (sessionLog == null || sessionLog.getDate() == null) {
            throw new IllegalArgumentException("SessionLog and its date cannot be null");
        }

        sessionLogs.compute(email, (key, existingLogs) -> {
            if (existingLogs == null) {
                List<SessionLog> newLogs = new CopyOnWriteArrayList<>();
                newLogs.add(sessionLog);
                return newLogs;
            }

            boolean updated = false;

            for (int i = 0; i < existingLogs.size(); i++) {
                if (existingLogs.get(i).getDate().toLocalDate().equals(sessionLog.getDate().toLocalDate())) {

                    existingLogs.set(i, sessionLog);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                existingLogs.add(sessionLog);
            }

            return existingLogs;
        });
    }
}
