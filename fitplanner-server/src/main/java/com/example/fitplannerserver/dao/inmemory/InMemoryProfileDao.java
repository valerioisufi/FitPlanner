package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.model.User;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProfileDao implements ProfileDao {

    // Map Key: userId
    private final Map<String, User> profiles = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");

        User userProfile = profiles.get(userId);

        return Optional.ofNullable(userProfile).map(User::new);
    }

    @Override
    public void save(User user) {
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(user.getId(), "userId cannot be null");

        User copyOfUser = new User(user);

        profiles.put(copyOfUser.getId(), copyOfUser);
    }

    @Override
    public Optional<User> findByInvitationCode(String invitationCode) {
        if (invitationCode == null || invitationCode.isBlank()) {
            return Optional.empty();
        }

        for (User user : profiles.values()) {
            if (user.getInvitationCode() != null && user.getInvitationCode().equals(invitationCode)) {
                return Optional.of(new User(user));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> getInvitationCode(String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(profiles.get(userId), "User profile not found for userId: " + userId);

        User user = profiles.get(userId);
        return Optional.ofNullable(user).map(User::getInvitationCode);
    }


}