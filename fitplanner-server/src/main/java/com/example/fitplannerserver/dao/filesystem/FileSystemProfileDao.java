package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.user.Account;
import com.example.fitplannerserver.model.user.AthleteUser;
import com.example.fitplannerserver.model.user.TrainerUser;
import com.example.fitplannerserver.model.user.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemProfileDao implements ProfileDao {

    private static final String CSV_HEADER = "userId,firstName,lastName,contactEmail,phoneNumber,profileType,invitationCode,trainerId";
    private static final int EXPECTED_COLUMNS = 8;
    private static final String USER_ID_CANNOT_BE_NULL = "userId cannot be null";

    private final Path path;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemProfileDao(Path path) {
        this.path = Objects.requireNonNull(path, "Il file non può essere nullo");
        initializeFile(this.path, CSV_HEADER);
    }

    @Override
    public Optional<User> findById(String userId) throws DaoException {
        Objects.requireNonNull(userId, USER_ID_CANNOT_BE_NULL);

        lock.readLock().lock();
        try {
            CsvResultSet rs = search(path, EXPECTED_COLUMNS, parts -> parts[0].equals(userId), 1);
            if (rs.next()) {
                return Optional.of(fromCsvRS(rs));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca del profilo", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(User user) throws DaoException {
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(user.getId(), USER_ID_CANNOT_BE_NULL);

        String newRow = toCsvRow(user);
        lock.writeLock().lock();

        try {
            update(path, EXPECTED_COLUMNS, parts -> parts[0].equals(user.getId()), newRow);

        } catch (IOException e) {
            throw new DaoException("Errore durante il salvataggio del profilo dell'utente", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<TrainerUser> findByInvitationCode(String invitationCode) throws DaoException {
        if (invitationCode == null || invitationCode.isBlank()) {
            return Optional.empty();
        }
        lock.readLock().lock();

        try {
            CsvResultSet rs = search(path, EXPECTED_COLUMNS, parts -> invitationCode.equals(parts[6]), 1);
            if (rs.next() && fromCsvRS(rs) instanceof TrainerUser trainer) {
                return Optional.of(trainer);
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca per invitation code", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<AthleteUser> findAthletesByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        lock.readLock().lock();
        try {
            List<AthleteUser> athletes = new ArrayList<>();
            CsvResultSet rs = search(path, EXPECTED_COLUMNS, parts -> trainerId.equals(parts[7]), -1);

            while (rs.next()) {
                if (fromCsvRS(rs) instanceof AthleteUser athlete) {
                    athletes.add(athlete);
                }
            }

            return athletes;
        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca degli atleti del trainer", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<AthleteUser> findAthleteById(String athleteId) throws DaoException {
        return findById(athleteId).filter(AthleteUser.class::isInstance).map(AthleteUser.class::cast);
    }

    @Override
    public Optional<TrainerUser> findTrainerById(String trainerId) throws DaoException {
        return findById(trainerId).filter(TrainerUser.class::isInstance).map(TrainerUser.class::cast);
    }

    // HELPER
    private String toCsvRow(User user) {
        return new CsvRowBuilder()
                .add(user.getId())
                .add(user.getFirstName())
                .add(user.getLastName())
                .add(user.getContactEmail())
                .add(user.getPhoneNumber())
                .add(user.getProfileType().name())
                .add(user instanceof TrainerUser t ? t.getInvitationCode() : null)
                .add(user instanceof AthleteUser a ? a.getTrainerId() : null)
                .build();
    }

    private User fromCsvRS(CsvResultSet rs) {
        Account.Role type = Account.Role.valueOf(rs.getString(5));
        if (type == Account.Role.TRAINER) {
            return new TrainerUser(rs.getString(0), rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(6));
        }
        return new AthleteUser(rs.getString(0), rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(7));
    }
}
