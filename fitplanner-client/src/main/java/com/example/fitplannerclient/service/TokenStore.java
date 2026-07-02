package com.example.fitplannerclient.service;

import java.util.prefs.Preferences;

/**
 * Custodisce i token di autenticazione (access + refresh)
 */
public class TokenStore {
    private static final String PREF_REFRESH_TOKEN = "refresh_token";

    private String accessToken;
    private String refreshToken;
    private final Preferences preferences;

    public TokenStore() {
        // il refresh token viene salvato nelle Preferences;
        // in un contesto reale andrebbe memorizzato tramite il keystore del SO
        preferences = Preferences.userNodeForPackage(TokenStore.class);

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

    public synchronized boolean hasTokens() {
        return accessToken != null || refreshToken != null;
    }

    public synchronized void clear() {
        setAccessToken(null);
        setRefreshToken(null);
    }
}
