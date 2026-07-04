package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannerserver.model.user.Account;
import com.example.fitplannerserver.model.user.User;

public class ProfileMapper {

    private ProfileMapper(){}

    public static ProfileDTO toBean(User user){
        return new ProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getContactEmail(),
                user.getProfileType() == Account.Role.ATHLETE ? ProfileDTO.ProfileType.ATHLETE : ProfileDTO.ProfileType.TRAINER
        );
    }

}
