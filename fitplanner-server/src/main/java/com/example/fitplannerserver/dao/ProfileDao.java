package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.User;

public interface ProfileDao {
    User findByEmail(String email);

    void save(String email, User user);
}
