package com.example.fitplannerclient.controller.profile;

import com.example.fitplannercommon.InvitationCodeBean;

public class TrainerProfileManager extends ProfileManager{
    String invitationCode;

    public void getInvitationCode(InvitationCodeBean bean){
        this.invitationCode = bean.getInvitationCode();
    }
}
