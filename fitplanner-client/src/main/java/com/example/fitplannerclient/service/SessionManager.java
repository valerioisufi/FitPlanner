package com.example.fitplannerclient.service;

import java.util.prefs.Preferences;

public class SessionManager {
    private static class Wrapper {
        public static final SessionManager INSTANCE = new SessionManager();
    }

    public static SessionManager getInstance() {
        return Wrapper.INSTANCE;
    }


    private static final String PREF_REFRESH_TOKEN = "refresh_token";

    private String accessToken;
    private String refreshToken;
    private final Preferences preferences;

    private SessionManager() {
        preferences = Preferences.userNodeForPackage(SessionManager.class);

        this.refreshToken = preferences.get(PREF_REFRESH_TOKEN, null);
    }

    public synchronized void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public synchronized void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;

        if (refreshToken != null) {
            preferences.put(PREF_REFRESH_TOKEN, refreshToken);
        } else {
            preferences.remove(PREF_REFRESH_TOKEN);
        }
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }
    public synchronized String getRefreshToken() {
        return refreshToken;
    }


    public synchronized boolean isLoggedIn() {
        return accessToken != null || refreshToken != null;
    }

    public synchronized void logout() {
        setAccessToken(null);
        setRefreshToken(null);
    }
}
