package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.user.AthleteUser;
import com.example.fitplannerserver.model.user.TrainerUser;
import com.example.fitplannerserver.model.user.User;

import java.util.List;
import java.util.Optional;

public interface ProfileDao {
    // Recupera un profilo (di qualsiasi ruolo) tramite id
    Optional<User> findById(String userId) throws DaoException;

    // Crea o aggiorna un profilo
    void save(User user) throws DaoException;

    // Trova un trainer tramite il suo codice di invito
    Optional<TrainerUser> findByInvitationCode(String invitationCode) throws DaoException;

    // Restituisce gli atleti seguiti da un trainer
    List<AthleteUser> findAthletesByTrainerId(String trainerId) throws DaoException;

    Optional<AthleteUser> findAthleteById(String athleteId) throws DaoException;

    Optional<TrainerUser> findTrainerById(String trainerId) throws DaoException;
}
