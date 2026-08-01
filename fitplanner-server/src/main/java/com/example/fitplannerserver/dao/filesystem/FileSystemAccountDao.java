package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.model.user.Account;

import java.io.IOException;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileSystemAccountDao implements AccountDao {

    private static final String CSV_HEADER = "userId,email,passwordHash,refreshToken,profileType";
    private static final int EXPECTED_COLUMNS = 5;

    private static final String ACCOUNT_CANNOT_BE_NULL = "account cannot be null";
    private static final String EMAIL_CANNOT_BE_NULL = "email cannot be null";
    private static final String USER_ID_CANNOT_BE_NULL = "userId cannot be null";

    private final Path file;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final FileSystemProfileDao profileDao;

    public FileSystemAccountDao(Path file, FileSystemProfileDao profileDao) {
        this.file = Objects.requireNonNull(file, "file cannot be null");
        CsvUtils.initializeFile(file, CSV_HEADER);

        this.profileDao = profileDao;
    }

    @Override
    public boolean create(Account account) throws DaoException {
        Objects.requireNonNull(account, ACCOUNT_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getEmail(), EMAIL_CANNOT_BE_NULL);

        String targetEmail= account.getEmail().toLowerCase();
        Account copyOfAccount = new Account(account);

        lock.writeLock().lock();
        try {
            boolean emailIsUsed = !CsvUtils.search(file, EXPECTED_COLUMNS, parts -> parts[1].equalsIgnoreCase(targetEmail), 1).isEmpty();

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
        Objects.requireNonNull(account, ACCOUNT_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getUserId(), USER_ID_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getEmail(), EMAIL_CANNOT_BE_NULL);

        Account copyOfAccount = new Account(account);
        String newRow = toCsvRow(copyOfAccount);

        lock.writeLock().lock();
        try {
            CsvUtils.update(file, EXPECTED_COLUMNS, parts -> parts[0].equals(copyOfAccount.getUserId()), newRow);

        } catch (IOException e) {
            throw new DaoException("Errore durante la modifica delle informazioni dell'account", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Account> findByEmail(String email) throws DaoException {
        Objects.requireNonNull(email, EMAIL_CANNOT_BE_NULL);

        String targetEmail = email.toLowerCase();

        lock.readLock().lock();
        try {
            CsvUtils.CsvResultSet rs = CsvUtils.search(file, EXPECTED_COLUMNS, parts -> parts[1].equalsIgnoreCase(targetEmail), 1);
            if(rs.next()){
                return Optional.of(fromCsvRS(rs));
            }
            return Optional.empty();

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
            CsvUtils.CsvResultSet rs = CsvUtils.search(file, EXPECTED_COLUMNS, parts -> parts[3].equals(refreshToken), 1);
            if(rs.next()){
                return Optional.of(fromCsvRS(rs));
            }
            return Optional.empty();

        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca del refresh token", e);
        } finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public void delete(Account account) throws DaoException {
        Objects.requireNonNull(account, ACCOUNT_CANNOT_BE_NULL);
        Objects.requireNonNull(account.getUserId(), USER_ID_CANNOT_BE_NULL);

        profileDao.delete(account.getUserId());

        lock.writeLock().lock();
        try {
            CsvUtils.delete(file, EXPECTED_COLUMNS, parts -> parts[0].equals(account.getUserId()));

        } catch (IOException e) {
            throw new DaoException("Errore durante la rimozione dell'account", e);
        } finally {
            lock.writeLock().unlock();
        }

    }


    //METODI HELPER

    private String toCsvRow(Account account) {
        return new CsvUtils.CsvRowBuilder()
                .add(account.getUserId())
                .add(account.getEmail())
                .add(account.getPasswordHash())
                .add(account.getRefreshToken())
                .add(account.getProfileType().name())
                .build();
    }

    private Account fromCsvRS(CsvUtils.CsvResultSet rs) {

        try {
            return new Account(
                    rs.getString(0),
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    Account.Role.valueOf(rs.getString(4))
            );

        } catch (IllegalArgumentException e) {
            throw new SystemException("Dati corrotti o ruolo non valido nel CSV per l'utente: " + rs.getString(0));
        }
    }
}
