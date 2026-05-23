package com.example.fitplannerclient.controller.profile;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.service.facade.ProfileFacade;
import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileManager {
    private final ProfileFacade profileFacade;
    private ProfileBean cachedProfile;
    private String previousUserId;

    public ProfileManager(ProfileFacade profileFacade){
        this.profileFacade = profileFacade;
    }

    public CompletableFuture<ProfileBean> getProfileInfoAsync() {
        return profileFacade.getProfileInfoAsync()
                .thenApply(this::dtoToBean)
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

    public ProfileBean getCachedProfile() {
        return cachedProfile;
    }

    public void clearCachedProfile() {
        this.previousUserId = (this.cachedProfile != null) ? this.cachedProfile.getUserId() : this.previousUserId;
        this.cachedProfile = null;
    }

    public CompletableFuture<Void> updateProfileInfoAsync(ProfileBean bean) {
        return profileFacade.updateProfileInfoAsync(beanToDto(bean));
    }

    public CompletableFuture<ProfileBean> getMyTrainerAsync() {
        return profileFacade.getMyTrainerAsync()
                .thenApply(this::dtoToBean);

    }

    public CompletableFuture<List<ProfileBean>> getMyAthletesAsync() {
        return profileFacade.getMyAthletesAsync()
                .thenApply(list -> list.stream().map(this::dtoToBean).toList());
    }

    public CompletableFuture<String> getInvitationCodeAsync(){
        return profileFacade.getInvitationCodeAsync().thenApply(InvitationCodeDTO::getInvitationCode);
    }

    public CompletableFuture<Void> linkTrainerAsync(String invitationCode){
        return profileFacade.linkTrainerAsync(new InvitationCodeDTO(invitationCode));
    }



    private ProfileDTO beanToDto(ProfileBean bean){
        ProfileDTO.ProfileType profileType =  switch(bean.getProfileType()){
            case TRAINER -> ProfileDTO.ProfileType.TRAINER;
            case ATHLETE -> ProfileDTO.ProfileType.ATHLETE;
            default ->
                    throw new IllegalArgumentException("Invalid profile type: " + bean.getProfileType());
        };

        return new ProfileDTO(
                bean.getUserId(),
                bean.getFirstName(),
                bean.getLastName(),
                bean.getPhoneNumber(),
                bean.getContactEmail(),
                profileType
        );

    }

    private ProfileBean dtoToBean(ProfileDTO dto){
        ProfileBean.ProfileType profileType = switch(dto.getProfileType()){
            case TRAINER -> ProfileBean.ProfileType.TRAINER;
            case ATHLETE -> ProfileBean.ProfileType.ATHLETE;
            default ->
                    throw new IllegalArgumentException("Invalid profile type: " + dto.getProfileType());
        };


        return new ProfileBean(
                dto.getUserId(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getPhoneNumber(),
                dto.getContactEmail(),
                profileType
        );
    }


}