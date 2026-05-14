package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.InvitationCodeBean;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.model.User;
import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannerserver.security.IdentityProvider;

import java.util.List;

public class ProfileController {
    private final IdentityProvider identityProvider;

    public ProfileController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public ProfileBean getProfileInfo() {
        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
        User user = profileDao.findByEmail(identityProvider.getUserId());

        ProfileBean profileBean = new ProfileBean(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),  //usiamo email dell'user e non dell'account
                user.getPhoneNumber(),
                user.getUsername()
        );

        return profileBean;
    }

    public void updateProfileInfo(ProfileBean profileBean) {
        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
        User user = profileDao.findByEmail(identityProvider.getUserId());

        user.setLastName(profileBean.getLastName());
        user.setName(profileBean.getFirstName());
        user.setEmail(profileBean.getContactEmail());
        user.setPhoneNumber(profileBean.getPhoneNumber());

        profileDao.save(identityProvider.getUserId(), user);

    }

    public ProfileBean getMyTrainer() {
    }

    public List<ProfileBean> getMyAthletes() {
    }

    public void linkTrainer(InvitationCodeBean invitationCodeBean){}

    public InvitationCodeBean generateNewInvitationCode() {
    }
}
