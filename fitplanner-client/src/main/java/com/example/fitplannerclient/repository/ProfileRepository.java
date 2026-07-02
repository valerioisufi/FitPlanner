package com.example.fitplannerclient.repository;

import com.example.fitplannerclient.entity.profile.Profile;
import com.example.fitplannerclient.exception.RequestException;
import com.example.fitplannerclient.service.api.ProfileApi;
import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ProfileRepository {

    private final ProfileApi profileApi;
    private Profile cachedProfile;

    public ProfileRepository(ProfileApi profileApi) {
        this.profileApi = profileApi;
    }


    public CompletableFuture<Profile> getProfileInfoAsync() {
        return profileApi.getProfileInfoAsync()
                .thenApply(this::dtoToEntity)
                .thenApply(profile -> {
                    this.cachedProfile = profile;
                    return profile;
                });
    }

    public Profile getCachedProfile() {
        return cachedProfile;
    }

    public CompletableFuture<Void> updateProfileInfoAsync(Profile entity) {
        return profileApi.updateProfileInfoAsync(entityToDto(entity))
                .thenRun(() -> this.cachedProfile = entity);
    }

    public CompletableFuture<String> getInvitationCodeAsync() {
        return profileApi.getInvitationCodeAsync()
                .thenApply(InvitationCodeDTO::getInvitationCode);
    }

    public CompletableFuture<Void> linkTrainerAsync(String invitationCode) {
        return profileApi.linkTrainerAsync(new InvitationCodeDTO(invitationCode));
    }

    public CompletableFuture<Profile> getMyTrainerAsync() {
        return profileApi.getMyTrainerAsync()
                .thenApply(this::dtoToEntity);
    }

    public CompletableFuture<Boolean> hasTrainerAsync() {
        return profileApi.getMyTrainerAsync()
                .thenApply(dto -> true)
                .exceptionally(ex -> {
                    Throwable cause = ex;
                    while (cause instanceof CompletionException && cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    if (cause instanceof RequestException requestException && requestException.getStatusCode() == 404) {
                        return false;
                    }
                    throw new CompletionException(ex);
                });
    }

    public CompletableFuture<List<Profile>> getMyAthletesAsync() {
        return profileApi.getMyAthletesAsync()
                .thenApply(list -> list.stream().map(this::dtoToEntity).toList());
    }

    private Profile dtoToEntity(ProfileDTO dto) {
        if (dto == null) return null;
        Profile.ProfileType profileType =  switch(dto.getProfileType()){
            case TRAINER -> Profile.ProfileType.TRAINER;
            case ATHLETE -> Profile.ProfileType.ATHLETE;
            default ->
                    throw new IllegalArgumentException("Invalid profile type: " + dto.getProfileType());
        };

        return new Profile(
                dto.getUserId(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getContactEmail(),
                dto.getPhoneNumber(),
                profileType
        );
    }

    private ProfileDTO entityToDto(Profile entity) {
        if (entity == null) return null;
        ProfileDTO.ProfileType profileType = switch(entity.getProfileType()){
            case TRAINER -> ProfileDTO.ProfileType.TRAINER;
            case ATHLETE -> ProfileDTO.ProfileType.ATHLETE;
            default ->
                    throw new IllegalArgumentException("Invalid profile type: " + entity.getProfileType());
        };

        return new ProfileDTO(
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getContactEmail(),
                entity.getPhoneNumber(),
                profileType
        );
    }

}
