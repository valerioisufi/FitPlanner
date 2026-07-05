package com.example.fitplannerclient.controller.session;

import com.example.fitplannerclient.bean.auth.LoginBean;
import com.example.fitplannerclient.bean.auth.RegisterBean;
import com.example.fitplannerclient.context.UserSessionContext;
import com.example.fitplannerclient.service.TokenStore;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Controller applicativo degli eventi di sessione: login, registrazione, logout,
 * ripresa della sessione persistita e scadenza dei token
 */
public class SessionManager {

    /**
     * Esito di un'autenticazione riuscita: se l'identità è la stessa della sessione
     * precedente le cache restano valide e la UI può riprendere da dove era
     */
    public enum LoginOutcome { SAME_USER, NEW_USER }

    private final AuthManager authManager;
    private final TokenStore tokenStore;
    private final Supplier<UserSessionContext> sessionFactory;

    private UserSessionContext session;
    private Supplier<CompletableFuture<LoginOutcome>> reauthenticationHandler;

    public SessionManager(AuthManager authManager, TokenStore tokenStore, Supplier<UserSessionContext> sessionFactory) {
        this.authManager = authManager;
        this.tokenStore = tokenStore;
        this.sessionFactory = sessionFactory;
    }

    public CompletableFuture<LoginOutcome> loginAsync(LoginBean loginBean) {
        return authManager.loginAsync(loginBean)
                .thenCompose(v -> openSessionAsync());
    }

    public CompletableFuture<LoginOutcome> registerAsync(RegisterBean registerBean) {
        return authManager.registerAsync(registerBean)
                .thenCompose(v -> openSessionAsync());
    }

    /**
     * Riapre la sessione con i token salvati in precedenza (avvio dell'applicazione).
     * Fallisce se il refresh token non è più valido (viene richiesto nuovamente il login)
     */
    public CompletableFuture<LoginOutcome> resumeSessionAsync() {
        return openSessionAsync();
    }

    private CompletableFuture<LoginOutcome> openSessionAsync() {
        UserSessionContext candidate = sessionFactory.get();

        return candidate.loadProfileAsync()
                .thenApply(profile -> {
                    if (session != null && session.getUserId().equals(profile.getUserId())) {
                        // stessa identità: il contesto già esistente e le sue cache restano validi
                        candidate.terminate(); // scarta il candidato
                        return LoginOutcome.SAME_USER;
                    }

                    if (session != null) {
                        session.terminate(); // chiudi la vecchia sessione se stiamo cambiando utente
                    }
                    
                    session = candidate;
                    return LoginOutcome.NEW_USER;
                });
    }

    public boolean isAuthenticated() {
        return session != null && tokenStore.hasTokens();
    }

    /** Esistono token salvati con cui provare a riprendere la sessione. */
    public boolean hasPersistedTokens() {
        return tokenStore.hasTokens();
    }

    public UserSessionContext getSession() {
        if (session == null) {
            throw new IllegalStateException("Nessun utente autenticato");
        }
        return session;
    }

    public void logout() {
        tokenStore.clear();
        if (session != null) {
            session.terminate();
            session = null; // tutte le cache specifiche dell'utente non devono essere mantenute
        }
    }

    /**
     * Registrato dalla UI: deve richiedere un nuovo login all'utente e completare
     * il future con l'esito. Se assente (CLI), la richiesta pendente fallisce.
     */
    public void setReauthenticationHandler(Supplier<CompletableFuture<LoginOutcome>> handler) {
        this.reauthenticationHandler = handler;
    }

    /**
     * Invocato da HttpService quando access e refresh token sono entrambi inutilizzabili.
     * Restituisce true solo se il re-login ha confermato la stessa identità: in quel caso
     * la richiesta pendente può essere ritentata; con un utente diverso deve fallire
     */
    public CompletableFuture<Boolean> onSessionExpired() {
        if (reauthenticationHandler == null) {
            return CompletableFuture.completedFuture(false);
        }
        return reauthenticationHandler.get()
                .thenApply(outcome -> outcome == LoginOutcome.SAME_USER);
    }
}
