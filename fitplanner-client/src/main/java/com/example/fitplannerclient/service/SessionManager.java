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

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;

        if (refreshToken != null) {
            preferences.put(PREF_REFRESH_TOKEN, refreshToken);
        } else {
            preferences.remove(PREF_REFRESH_TOKEN);
        }
    }

    public String getAccessToken() {
        return accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }


    public boolean isLoggedIn() {
        return accessToken != null || refreshToken != null;
    }

    public void logout() {
        setAccessToken(null);
        setRefreshToken(null);
    }
}
