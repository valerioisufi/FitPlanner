package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.User;

import java.util.Optional;

public interface ProfileDao {
    // Retrieves a profile by its unique ID
    Optional<User> findById(String userId) throws DaoException;

    // Updates existing profile information
    void save(User user) throws DaoException;

    // Find a trainer using their unique invitation code
    Optional<User> findByInvitationCode(String invitationCode) throws DaoException;

    Optional<String> getInvitationCode(String userId) throws DaoException;
}