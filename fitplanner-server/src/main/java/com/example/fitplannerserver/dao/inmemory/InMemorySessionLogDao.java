package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.model.SessionLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        return null;
    }

    @Override
    public void save(String email, SessionLog sessionLog) {

    }
}
