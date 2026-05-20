package com.example.fitplannerclient.controller.profile;

import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannerclient.entity.profile.Profile;

public abstract class ProfileManager {
    Profile profile;


    public void setProfileInformation(ProfileDTO bean){
            this.profile = new Profile(
                    bean.getFirstName(),
                    bean.getLastName(),
                    bean.getContactEmail(),
                    bean.getPhoneNumber()
            );
    }
}