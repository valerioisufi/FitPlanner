package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.InvitationCodeDTO;
import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannerserver.beanvalidator.ProfileValidator;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.ResourceNotFoundException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.mapper.ProfileMapper;
import com.example.fitplannerserver.model.user.Account;
import com.example.fitplannerserver.model.user.AthleteUser;
import com.example.fitplannerserver.model.user.TrainerUser;
import com.example.fitplannerserver.model.user.User;

import com.example.fitplannerserver.security.IdentityProvider;

import java.util.List;

public class ProfileController {
    private final IdentityProvider identityProvider;
    private final NotificationController notificationController;

    private final ProfileDao profileDao;
    private final WorkoutPlanDao workoutPlanDao;

    public ProfileController(
            IdentityProvider identityProvider,
            NotificationController notificationController,
            ProfileDao profileDao,
            WorkoutPlanDao workoutPlanDao
    ) {
        this.identityProvider = identityProvider;
        this.notificationController = notificationController;

        this.profileDao = profileDao;
        this.workoutPlanDao = workoutPlanDao;
    }

    public ProfileDTO getProfileInfo() {
        try {
            User user = profileDao.findById(identityProvider.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profilo non trovato"));

            return ProfileMapper.toBean(user);

        } catch (DaoException e) {
            throw new SystemException("Errore durante il recupero delle informazioni del profilo", e);
        }
    }

    public void updateProfileInfo(ProfileDTO profileDTO) {
        ProfileValidator.validateProfileBean(profileDTO);

        try {
            User user = profileDao.findById(identityProvider.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Errore durante l'aggiornamento delle informazioni del profilo. Profilo non trovato"));

            user.setUserProfileInfo(
                    profileDTO.getFirstName().trim(),
                    profileDTO.getLastName().trim(),
                    profileDTO.getContactEmail().trim(),
                    profileDTO.getPhoneNumber().trim()
            );

            profileDao.save(user);

        } catch (DaoException e) {
            throw new SystemException("Errore durante l'aggiornamento delle informazioni del profilo", e);
        }
    }

    public ProfileDTO getMyTrainer() {
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        try {
            String trainerId = profileDao.findAthleteById(identityProvider.getUserId())
                    .map(AthleteUser::getTrainerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Non hai un trainer assegnato"));

            return profileDao.findTrainerById(trainerId)
                    .map(ProfileMapper::toBean)
                    .orElseThrow(() -> new SystemException("Errore nel recuperare il profilo del trainer"));

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare il profilo del trainer", e);
        }
    }

    public List<ProfileDTO> getMyAthletes() {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        try {
            return profileDao.findAthletesByTrainerId(identityProvider.getUserId())
                    .stream()
                    .map(ProfileMapper::toBean)
                    .toList();

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare gli atleti del trainer", e);
        }
    }

    public void linkTrainer(InvitationCodeDTO invitationCodeDTO){
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        if(invitationCodeDTO == null || invitationCodeDTO.getInvitationCode() == null){
            throw new WrongArgumentsException("Il codice di invito non può essere nullo");
        }

        try {
            TrainerUser trainer = profileDao.findByInvitationCode(invitationCodeDTO.getInvitationCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Codice di invito non valido"));

            String athleteId = identityProvider.getUserId();
            if(trainer.getId().equals(athleteId)){
                // un atleta non può possedere un codice di invito, quindi non dovrebbe accadere
                throw new WrongArgumentsException("Non puoi collegarti a te stesso");
            }

            AthleteUser athlete = profileDao.findAthleteById(athleteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profilo atleta non trovato"));

            if (trainer.getId().equals(athlete.getTrainerId())) {
                // già collegato a questo trainer
                return;
            }

            if (athlete.getTrainerId() != null) {
                // cambio trainer: elimino il piano assegnato dal precedente
                workoutPlanDao.findAssignedPlanByAthleteId(athleteId)
                        .ifPresent(plan -> {
                            try {
                                workoutPlanDao.deletePlan(plan.getPlanId());
                            } catch (DaoException e) {
                                throw new SystemException("Errore durante la rimozione del piano precedente", e);
                            }
                        });
            }

            athlete.linkTo(trainer);
            profileDao.save(athlete);

            String notificationMsg = String.format("L'atleta %s %s si è collegato a te", athlete.getFirstName(), athlete.getLastName());
            notificationController.sendNotificationToUser(trainer.getId(), "NEW_ATHLETE_LINKED", notificationMsg);

        } catch (DaoException e){
            throw new SystemException("Errore durante il collegamento al trainer", e);
        }
    }

    public InvitationCodeDTO getInvitationCode() {
        identityProvider.checkUserRole(Account.Role.TRAINER);

        try {
            String invitationCode = profileDao.findTrainerById(identityProvider.getUserId())
                    .map(TrainerUser::getInvitationCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Codice di invito non trovato"));

            return new InvitationCodeDTO(invitationCode);
        } catch (DaoException e) {
            throw new SystemException("Errore durante il recupero del codice di invito", e);
        }
    }

}
