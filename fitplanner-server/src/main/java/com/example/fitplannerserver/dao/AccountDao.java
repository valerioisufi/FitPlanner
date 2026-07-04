package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.user.Account;

import java.util.Optional;

// Accounts are identified by userId; the email is a mutable, unique, case-insensitive attribute
public interface AccountDao {
    // Creates the account; returns false if the email is already in use
    boolean create(Account account) throws DaoException;

    // Updates the account identified by its userId
    void save(Account account) throws DaoException;

    Optional<Account> findByEmail(String email) throws DaoException;

    Optional<Account> findByRefreshToken(String refreshToken) throws DaoException;

    void delete(Account account) throws DaoException;
}