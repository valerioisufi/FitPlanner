package com.example.fitplannerclient.controller.profile;

import com.example.fitplannerclient.bean.ProfileBean;
import com.example.fitplannerclient.service.ProfileFacade;
import com.example.fitplannercommon.ProfileDTO;

import java.util.concurrent.CompletableFuture;

public class ProfileManager {
    private final ProfileFacade profileFacade;

    public ProfileManager(ProfileFacade profileFacade){
        this.profileFacade = profileFacade;
    }

    public CompletableFuture<ProfileBean> getProfileInfoAsync() {
        return profileFacade.getProfileInfoAsync()
                .thenApply(this::dtoToBean);
    }

    public CompletableFuture<Void> updateProfileInfoAsync(ProfileBean bean) {
        return profileFacade.updateProfileInfoAsync(beanToDto(bean));
    }
    public CompletableFuture<ProfileBean> getMyTrainerAsync() {
        return profileFacade.getMyTrainerAsync()
                .thenApply(this::dtoToBean);

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