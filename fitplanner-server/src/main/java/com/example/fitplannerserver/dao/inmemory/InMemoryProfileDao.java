package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProfileDao implements ProfileDao {

    private static class Wrapper {
        public static final InMemoryProfileDao INSTANCE = new InMemoryProfileDao();
    }

    public static InMemoryProfileDao getInstance() {
    return InMemoryProfileDao.Wrapper.INSTANCE;
}

    private final Map<String, User> profiles = new ConcurrentHashMap<>();

    @Override
    public User findByEmail(String email) {
        return profiles.get(email);
    }

    @Override
    public void save(User user) {
        profiles.put(user.getEmail(), user);
    }
}
