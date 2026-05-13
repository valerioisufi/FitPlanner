package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.Account;

public interface AccountDao {
    boolean create(Account account);

    void save(Account account);

    Account findById(String userId);

    Account findByEmail(String email);

    Account findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);

    void delete(Account account);
}
