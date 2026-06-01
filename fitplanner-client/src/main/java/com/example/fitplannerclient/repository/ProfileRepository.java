package com.example.fitplannerclient.repository;

import com.example.fitplannerclient.entity.profile.Profile;
import com.example.fitplannerclient.exception.RequestException;
import com.example.fitplannerclient.service.api.ProfileApi;
import com.example.fitplannercommon.ProfileDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ProfileRepository {

    private ProfileApi profileApi;
    private Profile cachedProfile;
    private String previousUserId;

    public ProfileRepository(ProfileApi profileApi) {
        this.profileApi = profileApi;
    }


    public CompletableFuture<Profile> getProfileInfoAsync() {
        return profileApi.getProfileInfoAsync()
                .thenApply(this::dtoToEntity)
                .thenApply(profile -> {
                    this.previousUserId = (this.cachedProfile != null) ? this.cachedProfile.getUserId() : this.previousUserId;
                    this.cachedProfile = profile;
                    return profile;
                });
    }

    public boolean didUserChange() {
        if (cachedProfile == null) return false;
        return previousUserId != null && !previousUserId.equals(cachedProfile.getUserId());
    }

    public Profile getCachedProfile() {
        return cachedProfile;
    }

    public void clearCachedProfile() {
        this.previousUserId = (this.cachedProfile != null) ? this.cachedProfile.getUserId() : this.previousUserId;
        this.cachedProfile = null;
    }

    public CompletableFuture<Void> updateProfileInfoAsync(Profile entity) {
        return profileApi.updateProfileInfoAsync(entityToDto(entity));
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
                    if (cause instanceof RequestException && ((RequestException) cause).getStatusCode() == 404) {
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
