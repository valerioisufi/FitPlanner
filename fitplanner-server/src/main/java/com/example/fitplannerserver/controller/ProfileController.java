package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannerserver.beanvalidator.ProfileValidator;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.ResourceNotFoundException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.mapper.ProfileMapper;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;
import com.example.fitplannerserver.security.IdentityProvider;

import java.util.ArrayList;
import java.util.List;

public class ProfileController {
    private final IdentityProvider identityProvider;

    private final ProfileDao profileDao;
    private final CoachingDao coachingDao;

    public ProfileController(
            IdentityProvider identityProvider,
            ProfileDao profileDao,
            CoachingDao coachingDao
    ) {
        this.identityProvider = identityProvider;

        this.profileDao = profileDao;
        this.coachingDao = coachingDao;
    }

    public ProfileDTO getProfileInfo() {

        try {
            User user = profileDao.findById(identityProvider.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profilo non trovato"));

            return ProfileMapper.toBean(user, identityProvider.getUserRole());

        } catch (DaoException e) {
            throw new SystemException("Errore durante il recupero delle informazioni del profilo", e);
        }
    }

    public void updateProfileInfo(ProfileDTO profileDTO) {
        ProfileValidator.validateProfileBean(profileDTO);

        try {
            User oldUser = profileDao.findById(identityProvider.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Errore durante l'aggiornamento delle informazioni del profilo. Profilo non trovato"));

            User newUser = new User(oldUser);
            newUser.setUserProfileInfo(
                    profileDTO.getFirstName().trim(),
                    profileDTO.getLastName().trim(),
                    profileDTO.getContactEmail().trim(),
                    profileDTO.getPhoneNumber().trim()
            );

            profileDao.save(newUser);

        } catch (DaoException e) {
            throw new SystemException("Errore durante l'aggiornamento delle informazioni del profilo", e);
        }

    }

    public ProfileDTO getMyTrainer() {
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        try {
            String trainerId = coachingDao.findTrainerIdByAthleteId(identityProvider.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Non hai un trainer assegnato"));

            User trainer = profileDao.findById(trainerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer non trovato"));

            return ProfileMapper.toBean(trainer, Account.Role.TRAINER);
        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare il profilo del trainer", e);
        }
    }

    public List<ProfileDTO> getMyAthletes() {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        try {
            List<String> athleteIds = coachingDao.findAthleteIdsByTrainerId(identityProvider.getUserId());

            List<ProfileDTO> athletes = new ArrayList<>();
            for(String athleteId : athleteIds) {
                User athlete = profileDao.findById(athleteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Atleta non trovato"));

                athletes.add(ProfileMapper.toBean(athlete, Account.Role.ATHLETE));
            }

            return athletes;

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare il profilo del trainer", e);
        }
    }

    public void linkTrainer(InvitationCodeDTO invitationCodeDTO){
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        if(invitationCodeDTO == null || invitationCodeDTO.getInvitationCode() == null){
            throw new WrongArgumentsException("Il codice di invito non può essere nullo");
        }

        try {
            User trainer = profileDao.findByInvitationCode(invitationCodeDTO.getInvitationCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Codice di invito non valido"));

            if(trainer.getId().equals(identityProvider.getUserId())){
                // un atleta non può possedere un codice di invito per cui questa eventualità non dovrebbe avvenire
                throw new WrongArgumentsException("Non puoi collegarti a te stesso");
            } else {
                coachingDao.linkAthleteToTrainer(identityProvider.getUserId(), trainer.getId());
            }

        } catch (DaoException e){
            throw new SystemException("Errore durante il collegamento al trainer", e);
        }
    }

    public InvitationCodeDTO getInvitationCode() {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        try {
            String invitationCode = profileDao.getInvitationCode(identityProvider.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Codice di invito non trovato"));

            return new InvitationCodeDTO(invitationCode);
        } catch (Exception e) {
            throw new SystemException("Errore durante il recupero del codice di invito", e);
        }
    }

}
