package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.model.Account;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountDao implements AccountDao {

    private static class Wrapper {
        public static final InMemoryAccountDao INSTANCE = new InMemoryAccountDao();
    }

    private InMemoryAccountDao(){}

    public static InMemoryAccountDao getInstance() {
        return Wrapper.INSTANCE;
    }

    // Map Key: email (Account.email)
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public boolean create(Account account) {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getEmail(), "Account email cannot be null");

        Account copyOfAccount = new Account(account);

        return accounts.putIfAbsent(copyOfAccount.getEmail().toLowerCase(), copyOfAccount) == null;
    }

    @Override
    public void save(Account account) {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getUserId(), "Account userId cannot be null");

        Account copyOfAccount = new Account(account);

        accounts.put(copyOfAccount.getUserId(), copyOfAccount);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        Objects.requireNonNull(email, "email cannot be null");

        Account account = accounts.get(email.toLowerCase());
        if (account != null) {
            return Optional.of(new Account(account));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }

        for (Account account : accounts.values()) {
            if (refreshToken.equals(account.getRefreshToken())) {
                return Optional.of(new Account(account));
            }
        }
        return Optional.empty();
    }

    @Override
    public void delete(Account account) {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getUserId(), "Account userId cannot be null");

        accounts.remove(account.getUserId());
    }
}