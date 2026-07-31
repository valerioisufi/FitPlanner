package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.model.user.Account;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountDao implements AccountDao {

    private static final String ACCOUNT_CANNOT_BE_NULL = "Account cannot be null";
    private static final String ACCOUNT_EMAIL_CANNOT_BE_NULL = "Account email cannot be null";
    private static final String ACCOUNT_USER_ID_CANNOT_BE_NULL = "Account userId cannot be null";

    // Map Key: userId
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    private final InMemoryProfileDao inMemoryProfileDao;
    public InMemoryAccountDao(InMemoryProfileDao inMemoryProfileDao) {
        this.inMemoryProfileDao = inMemoryProfileDao;
    }

    @Override
    public synchronized boolean create(Account account) {
        Objects.requireNonNull(account, ACCOUNT_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getUserId(), ACCOUNT_USER_ID_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getEmail(), ACCOUNT_EMAIL_CANNOT_BE_NULL);

        if (findByEmailInternal(account.getEmail()).isPresent()) {
            return false;
        }

        Account copyOfAccount = new Account(account);
        return accounts.putIfAbsent(copyOfAccount.getUserId(), copyOfAccount) == null;
    }

    @Override
    public void save(Account account) {
        Objects.requireNonNull(account, ACCOUNT_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getUserId(), ACCOUNT_USER_ID_CANNOT_BE_NULL);

        Account copyOfAccount = new Account(account);

        accounts.put(copyOfAccount.getUserId(), copyOfAccount);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        Objects.requireNonNull(email, "email cannot be null");

        return findByEmailInternal(email).map(Account::new);
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
        Objects.requireNonNull(account, ACCOUNT_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getUserId(), ACCOUNT_USER_ID_CANNOT_BE_NULL);

        inMemoryProfileDao.delete(account.getUserId());
        accounts.remove(account.getUserId());
    }

    private Optional<Account> findByEmailInternal(String email) {
        for (Account account : accounts.values()) {
            if (account.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(account);
            }
        }
        return Optional.empty();
    }
}
