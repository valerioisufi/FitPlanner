package com.example.fitplannerclient.controller.profile;

import com.example.fitplannerclient.exception.InvalidCodeException;
import com.example.fitplannercommon.InvitationCodeDTO;

public class AthleteProfileManager extends ProfileManager {

    public void addTrainerCode(InvitationCodeDTO bean) throws InvalidCodeException {
        String code= bean.getInvitationCode();
        // String athleteId = AthleteProfileServer.getAthleteId();

        if (code==null || code.isEmpty()){
            throw new InvalidCodeException("Il codice di invito non può essere vuoto.");
        }
//        service.sendInvitationCodeToServer(code, athleteId)
//                    }
//                });
    }

}
