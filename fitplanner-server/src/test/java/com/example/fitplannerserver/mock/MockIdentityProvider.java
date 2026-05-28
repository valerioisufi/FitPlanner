package com.example.fitplannerserver.mock;

import com.example.fitplannerserver.exception.ForbiddenException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.security.IdentityProvider;

public class MockIdentityProvider implements IdentityProvider {

    private String currentUserId;
    private Account.Role currentUserRole;

    public void setCurrentUser(String currentUserId, Account.Role currentUserRole) {
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
    }

    @Override
    public String getUserId() {
        return currentUserId;
    }

    @Override
    public Account.Role getUserRole() {
        return currentUserRole;
    }

    @Override
    public void checkUserRole(Account.Role expectedRole) {
        if (currentUserRole != expectedRole) {
            throw new ForbiddenException("Ruolo non autorizzato");
        }
    }
}
