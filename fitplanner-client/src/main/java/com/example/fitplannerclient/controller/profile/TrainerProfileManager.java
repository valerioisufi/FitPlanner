package com.example.fitplannerclient.controller.profile;

import com.example.fitplannercommon.InvitationCodeDTO;

public class TrainerProfileManager extends ProfileManager{
    String invitationCode;

    public void getInvitationCode(InvitationCodeDTO bean){
        this.invitationCode = bean.getInvitationCode();
    }
}
