package com.example.fitplannerclient.controller.profile;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.entity.profile.Profile;
import com.example.fitplannerclient.repository.ProfileRepository;
import com.example.fitplannerclient.service.api.ProfileApi;
import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileManager {
    private final ProfileApi profileApi;
    private final ProfileRepository profileRepository;

    public ProfileManager(ProfileApi profileApi, ProfileRepository profileRepository) {
        this.profileApi = profileApi;
        this.profileRepository = profileRepository;
    }

    public CompletableFuture<ProfileBean> getProfileInfoAsync() {
        return profileRepository.getProfileInfoAsync()
                .thenApply(this::entityToBean);
    }

    public ProfileBean getCacheProfileInfo(){
        Profile cachedProfile = profileRepository.getCachedProfile();
        if(cachedProfile == null) return null;

        return entityToBean(cachedProfile);
    }

    public CompletableFuture<Void> updateProfileInfoAsync(ProfileBean bean) {
        return profileRepository.updateProfileInfoAsync(beanToEntity(bean));
    }

    public CompletableFuture<ProfileBean> getMyTrainerAsync() {
        return profileRepository.getMyTrainerAsync()
                .thenApply(this::entityToBean);
    }

    public CompletableFuture<List<ProfileBean>> getMyAthletesAsync() {
        return profileRepository.getMyAthletesAsync()
                .thenApply(list -> list.stream().map(this::entityToBean).toList());
    }

    public CompletableFuture<String> getInvitationCodeAsync(){
        return profileApi.getInvitationCodeAsync().thenApply(InvitationCodeDTO::getInvitationCode);
    }

    public CompletableFuture<Void> linkTrainerAsync(String invitationCode){
        return profileApi.linkTrainerAsync(new InvitationCodeDTO(invitationCode));
    }


    private Profile beanToEntity(ProfileBean bean){
        Profile.ProfileType profileType =  switch(bean.getProfileType()){
            case TRAINER -> Profile.ProfileType.TRAINER;
            case ATHLETE -> Profile.ProfileType.ATHLETE;
            default ->
                    throw new IllegalArgumentException("Invalid profile type: " + bean.getProfileType());
        };

        return new Profile(
                bean.getUserId(),
                bean.getFirstName(),
                bean.getLastName(),
                bean.getPhoneNumber(),
                bean.getContactEmail(),
                profileType
        );

    }

    private ProfileBean entityToBean(Profile entity){
        ProfileBean.ProfileType profileType = switch(entity.getProfileType()){
            case TRAINER -> ProfileBean.ProfileType.TRAINER;
            case ATHLETE -> ProfileBean.ProfileType.ATHLETE;
            default ->
                    throw new IllegalArgumentException("Invalid profile type: " + entity.getProfileType());
        };


        return new ProfileBean(
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhoneNumber(),
                entity.getContactEmail(),
                profileType
        );
    }


}