package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.Account;

import java.util.Optional;

public interface AccountDao {
    boolean create(Account account) throws DaoException;

    void save(Account account) throws DaoException;

    Optional<Account> findByEmail(String email) throws DaoException;

    Optional<Account> findByRefreshToken(String refreshToken) throws DaoException;

    void delete(Account account) throws DaoException;
}