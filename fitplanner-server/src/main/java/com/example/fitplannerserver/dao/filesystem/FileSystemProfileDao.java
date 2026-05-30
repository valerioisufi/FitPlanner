package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.User;

import java.io.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemProfileDao implements ProfileDao {

    private static final String CSV_HEADER = "userId;firstName;lastName;contactEmail;phoneNumber;invitationCode";
    private static final int EXPECTED_COLUMNS = 6;

    private final File file;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemProfileDao(File file) {
        this.file = file;
    }

    @Override
    public Optional<User> findById(String userId) throws DaoException {
        Objects.requireNonNull(userId, "userId cannot be null");

        lock.readLock().lock();

        try {
            return search(file, EXPECTED_COLUMNS, parts -> parts[0].equals(userId), 1)
                    .stream()
                    .findFirst()
                    .map(this::fromCsvRow);

        } catch (IOException e){
            throw new DaoException("Errore durante la ricerca del profilo", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(User user) throws DaoException {
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(user.getId(), "userId cannot be null");

        String newRow = toCsvRow(user);
        lock.writeLock().lock();

        try {
            update(file, EXPECTED_COLUMNS, parts -> parts[0].equals(user.getId()), newRow);

        } catch (IOException e) {
            throw new DaoException("Errore durante il salvataggio del profilo dell'utente", e);
        } finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public Optional<User> findByInvitationCode(String invitationCode) throws DaoException {
        return Optional.empty();
    }

    @Override
    public Optional<String> getInvitationCode(String userId) throws DaoException {
        return Optional.empty();
    }

    private String toCsvRow(User profile) {

        return String.join(CSV_DELIMITER,
                convertNullToEmptyString(profile.getId()),
                convertNullToEmptyString(profile.getFirstName()),
                convertNullToEmptyString(profile.getLastName()),
                convertNullToEmptyString(profile.getContactEmail()),
                convertNullToEmptyString(profile.getPhoneNumber()),
                convertNullToEmptyString(profile.getInvitationCode())
        );
    }

    private User fromCsvRow(String[] parts) {

        return new User(
                convertEmptyStringToNull(parts[0]),
                convertEmptyStringToNull(parts[1]),
                convertEmptyStringToNull(parts[2]),
                convertEmptyStringToNull(parts[3]),
                convertEmptyStringToNull(parts[4]),
                convertEmptyStringToNull(parts[5])
        );

    }
}
