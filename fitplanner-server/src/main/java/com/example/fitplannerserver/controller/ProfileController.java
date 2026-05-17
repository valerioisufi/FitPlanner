package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.InvitationCodeBean;
import com.example.fitplannerserver.beanvalidator.ProfileValidator;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.*;
import com.example.fitplannerserver.mapper.ProfileMapper;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;
import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannerserver.security.IdentityProvider;

import java.util.ArrayList;
import java.util.List;

public class ProfileController {
    private final IdentityProvider identityProvider;

    public ProfileController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public ProfileBean getProfileInfo() {
        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();

        try {
            User user = profileDao.findById(identityProvider.getUserId())
                    .orElseThrow(() -> new SystemException("Profilo non trovato"));

            return ProfileMapper.toBean(user, identityProvider.getUserRole());

        } catch (DaoException e) {
            throw new SystemException("Errore durante il recupero delle informazioni del profilo");
        }
    }

    public void updateProfileInfo(ProfileBean profileBean) {
        ProfileValidator.validateProfileBean(profileBean);

        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();

        try {
            User oldUser = profileDao.findById(identityProvider.getUserId())
                    .orElseThrow(() -> new SystemException("Errore durante l'aggiornamento delle informazioni del profilo. Profilo non trovato"));

            User newUser = new User(oldUser);
            newUser.setUserProfileInfo(
                    profileBean.getUsername().trim(),
                    profileBean.getFirstName().trim(),
                    profileBean.getLastName().trim(),
                    profileBean.getContactEmail().trim(),
                    profileBean.getPhoneNumber().trim()
            );

            profileDao.save(newUser);

        } catch (DaoException e) {
            throw new SystemException("Errore durante l'aggiornamento delle informazioni del profilo");
        }

    }

    public ProfileBean getMyTrainer() {
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        CoachingDao coachingDao = DaoFactory.getInstance().getCoachingDao();
        try {
            String trainerId = coachingDao.findTrainerIdByAthleteId(identityProvider.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Non hai un trainer assegnato"));

            ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
            User trainer = profileDao.findById(trainerId)
                    .orElseThrow(() -> new SystemException("Trainer non trovato"));

            return ProfileMapper.toBean(trainer, Account.Role.TRAINER);
        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare il profilo del trainer");
        }
    }

    public List<ProfileBean> getMyAthletes() {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        CoachingDao coachingDao = DaoFactory.getInstance().getCoachingDao();
        try {
            List<String> athleteIds = coachingDao.findAthleteIdsByTrainerId(identityProvider.getUserId());

            ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();

            List<ProfileBean> athletes = new ArrayList<>();
            for(String athleteId : athleteIds) {
                User athlete = profileDao.findById(athleteId)
                        .orElseThrow(() -> new SystemException("Atleta non trovato"));

                athletes.add(ProfileMapper.toBean(athlete, Account.Role.ATHLETE));
            }

            return athletes;

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare il profilo del trainer");
        }
    }

    public void linkTrainer(InvitationCodeBean invitationCodeBean){
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        if(invitationCodeBean == null || invitationCodeBean.getInvitationCode() == null){
            throw new WrongArgumentsException("Il codice di invito non può essere nullo");
        }

        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
        try {
            User trainer = profileDao.findByInvitationCode(invitationCodeBean.getInvitationCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Codice di invito non valido"));

            if(trainer.getId().equals(identityProvider.getUserId())){
                // un atleta non può possedere un codice di invito per cui questa eventualità non dovrebbe avvenire
                throw new SystemException("Non puoi collegarti a te stesso");
            } else {
                CoachingDao coachingDao = DaoFactory.getInstance().getCoachingDao();
                coachingDao.linkAthleteToTrainer(identityProvider.getUserId(), trainer.getId());
            }

        } catch (DaoException e){
            throw new SystemException("Errore durante il collegamento al trainer");
        }
    }

    public InvitationCodeBean getInvitationCode() {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
        try {
            String invitationCode = profileDao.getInvitationCode(identityProvider.getUserId())
                    .orElseThrow(() -> new SystemException("Codice di invito non trovato"));

            return new InvitationCodeBean(invitationCode);
        } catch (Exception e) {
            throw new SystemException("Errore durante il recupero del codice di invito");
        }
    }

}
