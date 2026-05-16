package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;

public class ProfileMapper {

    private ProfileMapper(){}

    public static ProfileBean toBean(User user, Account.Role role){
        return new ProfileBean(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getContactEmail(),
                role == Account.Role.ATHLETE ? ProfileBean.ProfileType.ATHLETE : ProfileBean.ProfileType.TRAINER
        );
    }

}
