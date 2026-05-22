package com.example.fitplannerclient.service.facade;

import com.example.fitplannerclient.service.HttpService;
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
     */
    public CompletableFuture<InvitationCodeDTO> getInvitationCodeAsync() {
        return httpService.getAsync("/profiles/my-code", InvitationCodeDTO.class);
    }
}