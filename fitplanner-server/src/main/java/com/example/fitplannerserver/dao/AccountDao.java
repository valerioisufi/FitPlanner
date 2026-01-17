package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.Account;

public interface AccountDao {
    boolean create(Account account);

    void save(Account account);

    Account findByUsername(String username);

    Account findByRefreshToken(String refreshToken);

    boolean existsByUsername(String username);

    void delete(Account account);
}
