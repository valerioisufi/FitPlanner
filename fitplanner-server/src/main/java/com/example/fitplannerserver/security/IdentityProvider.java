package com.example.fitplannerserver.security;

import com.example.fitplannerserver.model.user.Account;

public interface IdentityProvider {
    String getUserId();

    Account.Role getUserRole();

    void checkUserRole(Account.Role role);
}
