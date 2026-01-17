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
        return accounts.putIfAbsent(account.getUsername(), account) == null;
    }

    @Override
    public void save(Account account) {
        accounts.put(account.getUsername(), account);
    }

    @Override
    public Account findByUsername(String username) {
        return accounts.get(username);
    }

    @Override
    public Account findByRefreshToken(String refreshToken) {
        for (Account account : accounts.values()) {
            if (account.getRefreshToken().equals(refreshToken)) {
                return account;
            }
        }
        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return accounts.containsKey(username);
    }

    @Override
    public void delete(Account account) {
        accounts.remove(account.getUsername());
    }
}
