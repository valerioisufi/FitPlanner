package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.InvitationCodeBean;
import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.*;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;
import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannerserver.security.IdentityProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

            return toBean(user, identityProvider.getUserRole());

        } catch (DaoException e) {
            throw new SystemException("Errore durante il recupero delle informazioni del profilo");
        }
    }

    public void updateProfileInfo(ProfileBean profileBean) {
        checkProfileBean(profileBean);

        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();

        try {
            User oldUser = profileDao.findById(identityProvider.getUserId())
                    .orElseThrow(() -> new SystemException("Errore durante l'aggiornamento delle informazioni del profilo. Profilo non trovato"));

            User newUser = new User(oldUser);
            newUser.setUserProfileInfo(
                    profileBean.getUsername(),
                    profileBean.getFirstName(),
                    profileBean.getLastName(),
                    profileBean.getContactEmail(),
                    profileBean.getPhoneNumber()
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

            return toBean(trainer, Account.Role.TRAINER);
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
                        .orElseThrow(() -> new SystemException("Trainer non trovato"));

                athletes.add(toBean(athlete, Account.Role.ATHLETE));
            }

            return athletes;

        } catch (DaoException e){
            throw new SystemException("Errore nel recuperare il profilo del trainer");
        }
    }

    public void linkTrainer(InvitationCodeBean invitationCodeBean){
        identityProvider.checkUserRole(Account.Role.ATHLETE);

        ProfileDao profileDao = DaoFactory.getInstance().getProfileDao();
        try {
            User trainer = profileDao.findByInvitationCode(invitationCodeBean.getInvitationCode())
                    .orElseThrow(() -> new SystemException("Codice di invito non valido"));

            if(trainer.getId().equals(identityProvider.getUserId())){
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

    // mapper
    private ProfileBean toBean(User user, Account.Role role){
        return new ProfileBean(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getContactEmail(),
                role == Account.Role.ATHLETE ? ProfileBean.ProfileType.ATHLETE : ProfileBean.ProfileType.TRAINER
        );
    }

    private void checkProfileBean(ProfileBean bean){
        if(bean.getFirstName() == null || bean.getLastName() == null || bean.getUsername() == null || bean.getPhoneNumber() == null || bean.getContactEmail() == null){
            throw new WrongArgumentsException("Non sono ammessi valori nulli per i campi firstName, lastName, username, phoneNumber e contactEmail");
        }

        if (bean.getUsername().trim().length() < 3 || bean.getUsername().trim().length() > 30) {
            throw new WrongArgumentsException("Lo username deve essere compreso tra 3 e 30 caratteri");
        }

        if (bean.getFirstName().trim().length() > 50) {
            throw new WrongArgumentsException("Il nome non può superare 50 caratteri");
        }

        if (bean.getLastName().trim().length() > 50) {
            throw new WrongArgumentsException("Il cognome non può superare 50 caratteri");
        }

        String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
        String email = bean.getContactEmail();
        if(!emailPattern.matcher(email).matches()){
            throw new WrongArgumentsException("inserire un'email valida");
        }

        if (bean.getContactEmail().trim().length() > 254) {
            throw new WrongArgumentsException("L'email non può superare 100 caratteri");
        }

        String phone = bean.getPhoneNumber();
        if (phone != null && !phone.trim().isEmpty()) {
            if (!phone.matches("^[+]?[0-9]{8,15}$")) {
                throw new WrongArgumentsException("Phone number format is invalid");
            }
        }
    }


}
