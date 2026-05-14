package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.model.Account;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountDao implements AccountDao {

    private static class Wrapper {
        public static final InMemoryAccountDao INSTANCE = new InMemoryAccountDao();
    }

    public static InMemoryAccountDao getInstance() {
        return Wrapper.INSTANCE;
    }

    // Map Key: email (Account.email)
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public boolean create(Account account) {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getUserId(), "Account userId cannot be null");

        Account copyOfAccount = new Account(account);

        return accounts.putIfAbsent(copyOfAccount.getUserId(), copyOfAccount) == null;
    }

    @Override
    public void save(Account account) {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getUserId(), "Account userId cannot be null");

        Account copyOfAccount = new Account(account);

        accounts.put(copyOfAccount.getUserId(), copyOfAccount);
    }

    @Override
    public Account findById(String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");

        Account foundAccount = accounts.get(userId);

        if (foundAccount == null) {
            return null;
        }

        return new Account(foundAccount);
    }

    @Override
    public Account findByEmail(String email) {
        Objects.requireNonNull(email, "email cannot be null");

        for (Account account : accounts.values()) {
            if (email.equalsIgnoreCase(account.getEmail())) {
                return new Account(account);
            }
        }
        return null;
    }

    @Override
    public Account findByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }

        for (Account account : accounts.values()) {
            if (refreshToken.equals(account.getRefreshToken())) {
                return new Account(account);
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
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getUserId(), "Account userId cannot be null");

        accounts.remove(account.getUserId());
    }
}