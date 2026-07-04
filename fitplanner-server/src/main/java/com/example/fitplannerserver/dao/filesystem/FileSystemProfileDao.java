package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.User;

import java.io.IOException;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemProfileDao implements ProfileDao {

    private static final String CSV_HEADER = "userId;firstName;lastName;contactEmail;phoneNumber;invitationCode";
    private static final int EXPECTED_COLUMNS = 6;
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
            return search(path, EXPECTED_COLUMNS, parts -> parts[0].equals(userId), 1)
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
    public Optional<User> findByInvitationCode(String invitationCode) throws DaoException {
        if(invitationCode == null || invitationCode.isBlank()){
            return Optional.empty();
        }
        lock.readLock().lock();

        try {
            return search(path, EXPECTED_COLUMNS, parts -> parts[5].equals(invitationCode),1).stream().findFirst().map(this::fromCsvRow);
        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca per invitation code", e);
        }finally {
            lock.readLock().unlock();
        }
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
