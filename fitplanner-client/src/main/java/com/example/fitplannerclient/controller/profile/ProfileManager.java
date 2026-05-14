package com.example.fitplannerclient.controller.profile;

import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannerclient.entity.profile.Profile;

public abstract class ProfileManager {
    Profile profile;


    public void setProfileInformation(ProfileBean bean){
            this.profile = new Profile(
                    bean.getUsername(),
                    bean.getFirstName(),
                    bean.getLastName(),
                    bean.getContactEmail(),
                    bean.getPhoneNumber()
            );
    }
}