package com.example.fitplannerclient.service.api;

import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProfileApi {
    CompletableFuture<ProfileDTO> getProfileInfoAsync();
    CompletableFuture<Void> updateProfileInfoAsync(ProfileDTO profileDTO);
    CompletableFuture<ProfileDTO> getMyTrainerAsync();
    CompletableFuture<List<ProfileDTO>> getMyAthletesAsync();
    CompletableFuture<Void> linkTrainerAsync(InvitationCodeDTO invitationCodeDTO);
    CompletableFuture<InvitationCodeDTO> getInvitationCodeAsync();
}
