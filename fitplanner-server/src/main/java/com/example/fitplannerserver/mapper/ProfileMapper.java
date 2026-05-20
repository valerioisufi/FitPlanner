package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;

public class ProfileMapper {

    private ProfileMapper(){}

    public static ProfileDTO toBean(User user, Account.Role role){
        return new ProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getContactEmail(),
                role == Account.Role.ATHLETE ? ProfileDTO.ProfileType.ATHLETE : ProfileDTO.ProfileType.TRAINER
        );
    }

}
