package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.model.Account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountDao implements AccountDao {

    private static class Wrapper {
        public static final InMemoryAccountDao INSTANCE = new InMemoryAccountDao();
    }

    public static InMemoryAccountDao getInstance() {
        return Wrapper.INSTANCE;
    }

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public boolean create(Account account) {
        return accounts.putIfAbsent(account.getUserId(), account) == null;
    }

    @Override
    public void save(Account account) {
        accounts.put(account.getUserId(), account);
    }

    @Override
    public Account findById(String userId) {
        return accounts.get(userId);
    }

    @Override
    public Account findByEmail(String email) {
        for (Account account : accounts.values()) {
            if (account.getEmail().equalsIgnoreCase(email)) {
                return account;
            }
        }
        return null;
    }

    @Override
    public Account findByRefreshToken(String refreshToken) {
        for (Account account : accounts.values()) {
            if (refreshToken != null && refreshToken.equals(account.getRefreshToken())) {
                return account;
            }
        }
        return null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public void delete(Account account) {
        accounts.remove(account.getUserId());
    }
}