package com.example.fitplannerserver.controller;

import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.model.User;
import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannerserver.security.IdentityProvider;

public class ProfileController {
    private final IdentityProvider identityProvider;

    public ProfileController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public void getProfileInfo() {
        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
        User user = profileDao.findByEmail(identityProvider.getEmail());

        ProfileBean profileBean = new ProfileBean(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),  //usiamo email dell'user e non dell'account
                user.getPhoneNumber(),
                user.getUsername()
        );
    }

    public void updateProfileInfo(ProfileBean profileBean) {
        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
        User user = profileDao.findByEmail(identityProvider.getEmail());

        

    }
}
