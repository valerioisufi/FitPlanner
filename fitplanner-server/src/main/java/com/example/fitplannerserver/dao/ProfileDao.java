package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.User;

public interface ProfileDao {
    public User findByEmail(String email);

    public void save(User user);
}
