package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.model.Account;

import java.io.*;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileSystemAccountDao implements AccountDao {

    private static final String CSV_DELIMITER = ";";
    private static final String CSV_HEADER = "userId;email;passwordHash;refreshToken;profileType";
    private static final int EXPECTED_COLUMNS = 5;

    private final File file;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemAccountDao(File file) {
        this.file = Objects.requireNonNull(file, "file cannot be null");
        CsvUtils.initializeFile(file, CSV_HEADER);
    }

    @Override
    public boolean create(Account account) throws DaoException {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(account.getEmail(), "email cannot be null");

        String targetEmail= account.getEmail().toLowerCase();
        Account copyOfAccount = new Account(account);

        lock.writeLock().lock();
        try {
            boolean emailIsUsed = CsvUtils.search(file, EXPECTED_COLUMNS, parts -> parts[1].equalsIgnoreCase(targetEmail), 1)
                    .stream()
                    .findFirst()
                    .isPresent();

            if (emailIsUsed) {
                return false;
            }

            CsvUtils.append(file, toCsvRow(copyOfAccount));
            return true;

        } catch (IOException e) {
            throw new DaoException("Errore durante la creazione dell'account", e);
        } finally {
            lock.writeLock().unlock();
        }


    }

    @Override
    public void save(Account account) throws DaoException {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(account.getEmail(), "email cannot be null");

        Account copyOfAccount = new Account(account);
        String newRow = toCsvRow(copyOfAccount);

        lock.writeLock().lock();
        try {
            CsvUtils.update(file, EXPECTED_COLUMNS, parts -> parts[1].equalsIgnoreCase(copyOfAccount.getEmail()), newRow);

        } catch (IOException e) {
            throw new DaoException("Errore durante la modifica delle informazioni dell'account", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Account> findByEmail(String email) throws DaoException {
        Objects.requireNonNull(email, "email cannot be null");

        String targetEmail = email.toLowerCase();

        lock.readLock().lock();
        try {
            return CsvUtils.search(file, EXPECTED_COLUMNS, parts -> parts[1].equalsIgnoreCase(targetEmail), 1)
                    .stream()
                    .findFirst()
                    .map(this::fromCsvRow);

        } catch (IOException e){
            throw new DaoException("Errore durante la ricerca dell'account", e);
        }finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public Optional<Account> findByRefreshToken(String refreshToken) throws DaoException {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }

        lock.readLock().lock();

        try  {
            return CsvUtils.search(file, EXPECTED_COLUMNS, parts -> parts[3].equals(refreshToken), 1)
                    .stream()
                    .findFirst()
                    .map(this::fromCsvRow);

        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca del refresh token", e);
        } finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public void delete(Account account) throws DaoException {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(account.getEmail(), "email cannot be null");

        Account copyOfAccount= new Account(account);

        lock.writeLock().lock();
        try {
            CsvUtils.delete(file, EXPECTED_COLUMNS, parts -> parts[1].equalsIgnoreCase(copyOfAccount.getEmail()));

        } catch (IOException e) {
            throw new DaoException("Errore durante la rimozione dell'account", e);
        } finally {
            lock.writeLock().unlock();
        }

    }


    //METODI HELPER

    private String toCsvRow(Account account) {
        String token = account.getRefreshToken() != null ? account.getRefreshToken() : "";

        return String.join(CSV_DELIMITER,
                account.getUserId(),
                account.getEmail(),
                account.getPasswordHash(),
                token,
                account.getProfileType().name()
        );
    }

    private Account fromCsvRow(String[] parts) {

        try {
            String token = parts[3].isEmpty() ? null : parts[3];

            return new Account(
                    parts[0], // userId
                    parts[1], // email
                    parts[2], // password
                    token,
                    Account.Role.valueOf(parts[4])
            );

        } catch (IllegalArgumentException e) {
            throw new SystemException("Dati corrotti o ruolo non valido nel CSV per l'utente: " + parts[0]);
        }
    }
}
