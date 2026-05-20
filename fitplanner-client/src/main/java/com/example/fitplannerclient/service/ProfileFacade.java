package com.example.fitplannerclient.service;

import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileFacade {

    private final HttpService httpService;

    public ProfileFacade(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Recupera le informazioni del profilo dell'utente correntemente autenticato.
     */
    public CompletableFuture<ProfileDTO> getProfileInfoAsync() {
        return httpService.getAsync("/profiles/me", ProfileDTO.class);
    }

    /**
     * Aggiorna le informazioni del profilo dell'utente correntemente autenticato.
     */
    public CompletableFuture<Void> updateProfileInfoAsync(ProfileDTO profileDTO) {
        return httpService.putAsync("/profiles/me", profileDTO, Void.class);
    }

    /**
     * Permette a un atleta di recuperare il profilo del suo trainer.
     */
    public CompletableFuture<ProfileDTO> getMyTrainerAsync() {
        return httpService.getAsync("/profiles/my-trainer", ProfileDTO.class);
    }

    /**
     * Permette a un trainer di recuperare la lista dei suoi atleti.
     * Si usa l'array di DTO per aggirare la type erasure sui generics e poi si mappa a List.
     */
    public CompletableFuture<List<ProfileDTO>> getMyAthletesAsync() {
        return httpService.getAsync("/profiles/my-athletes", ProfileDTO[].class)
                .thenApply(Arrays::asList);
    }

    /**
     * Permette a un atleta di collegarsi a un trainer utilizzando un codice di invito.
     */
    public CompletableFuture<Void> linkTrainerAsync(InvitationCodeDTO invitationCodeDTO) {
        return httpService.postAsync("/profiles/my-trainer/link", invitationCodeDTO, Void.class);
    }

    /**
     * Permette a un trainer di generare o recuperare il proprio codice di invito.
     * Dato che il backend prevede una POST senza un @RequestBody, passiamo null come body.
     */
    public CompletableFuture<InvitationCodeDTO> getInvitationCodeAsync() {
        return httpService.postAsync("/profiles/my-code", null, InvitationCodeDTO.class);
    }
}