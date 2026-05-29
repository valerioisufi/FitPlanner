package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.Account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileSystemAccountDao implements AccountDao {

    private static final String CSV_DELIMITER = ";";
    private static final String CSV_HEADER = "userId;email;passwordHash;refreshToken;profileType";
    private static final int EXPECTED_COLUMNS = 5;

    private final Path filePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemAccountDao(Path filePath) throws DaoException {
        this.filePath = Objects.requireNonNull(filePath, "filePath cannot be null");
        inizializedFile();
    }

    @Override
    public boolean create(Account account) throws DaoException {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(account.getEmail(), "email cannot be null");

        String targetEmail= account.getEmail().toLowerCase();
        Account copyOfAccount = new Account(account);

        lock.writeLock().lock();
        try{
            boolean emailExist;
            try (var lines = Files.lines(filePath)){
                emailExist = lines.skip(1).map(line->line.split(CSV_DELIMITER, -1)).filter(parts->parts.length>=2)
                        .anyMatch(parts->parts[1].equalsIgnoreCase(targetEmail));
            }
            if (emailExist){
                return false;
            }
            String newRow= toCsvRow(copyOfAccount)+System.lineSeparator();
            Files.writeString(filePath, newRow, StandardOpenOption.APPEND);
            return true;
        }catch (IOException e){
            throw new DaoException("Errore durante la creazione dell'account", e);
        }finally {
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
            java.util.List<String> allLines = Files.readAllLines(filePath);
            boolean updated = false;

            for (int i = 1; i < allLines.size(); i++) {
                Account existing = fromCsvRow(allLines.get(i));
                if (existing.getEmail().equals(copyOfAccount.getEmail())) {
                    allLines.set(i, newRow);
                    updated = true;
                    break;
                }
            }
            if (updated) {
                Files.write(filePath, allLines);
            } else {
                Files.writeString(filePath, newRow + System.lineSeparator(), StandardOpenOption.APPEND);
            }
        }catch (IOException e){
            throw new DaoException("Errore durante la modifica delle informazioni dell'account", e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public java.util.Optional<Account> findByEmail(String email) throws DaoException {
        Objects.requireNonNull(email, "email cannot be null");

        String targetEmail = email.toLowerCase();

        lock.readLock().lock();
        try (java.io.BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                Account existing = fromCsvRow(line);
                if (targetEmail.equals(existing.getEmail())) {
                    return java.util.Optional.of(new Account(existing));
                }
            }
        } catch (IOException e){
            throw new DaoException("Errore durante la ricerca dell'account", e);
        }finally {
            lock.readLock().unlock();
        }
        return Optional.empty();
    }

    @Override
    public java.util.Optional<Account> findByRefreshToken(String refreshToken) throws DaoException {
        if (refreshToken == null || refreshToken.isBlank()) {
            return java.util.Optional.empty();
        }

        lock.readLock().lock();
        try (java.io.BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                Account existing = fromCsvRow(line);
                if (refreshToken.equals(existing.getRefreshToken())) {
                    return java.util.Optional.of(new Account(existing));
                }
            }

        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca del refresh token", e);
        } finally {
            lock.readLock().unlock();
        }

        return java.util.Optional.empty();
    }

    @Override
    public void delete(Account account) throws DaoException {
        Objects.requireNonNull(account, "account cannot be null");
        Objects.requireNonNull(account.getEmail(), "email cannot be null");

        Account copyOfAccount= new Account(account);

        lock.writeLock().lock();
        try {
            List<String> allLines = Files.readAllLines(filePath);
            boolean isDeleted = false;
            for (int i = 1; i < allLines.size(); i++) {
                Account existing = fromCsvRow(allLines.get(i));
                if (existing.getEmail().equals(copyOfAccount.getEmail())) {
                    allLines.remove(i);
                    isDeleted = true;
                    break;
                }
            }
            if (isDeleted) {
                    Files.write(filePath, allLines);
            }
        }catch (IOException e){
            throw new DaoException("Errore durante errore durante la rimozione dell'account", e);
        }finally {
            lock.writeLock().unlock();
        }
    }




    //METODI HELPER
    private void inizializedFile() throws DaoException {
        lock.writeLock().lock();
        try{
            if(Files.notExists(filePath)){
                if(filePath.getParent()!=null){
                    Files.createDirectories(filePath.getParent());
                }
                Files.writeString(filePath, CSV_HEADER + System.lineSeparator(), StandardOpenOption.CREATE);
            }
        }catch (IOException e){
            throw new DaoException("Impossibile inizializzare il file CSV nel percorso " + filePath, e);
        }finally {
            lock.writeLock().unlock();
        }
    }

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

    private Account fromCsvRow(String line) throws DaoException {
        String[] parts = line.split(CSV_DELIMITER, -1);

        if (parts.length != EXPECTED_COLUMNS) {
            throw new DaoException("Riga CSV malformata. Attese " + EXPECTED_COLUMNS + " colonne, trovate " + parts.length);
        }

        try {
            String token = parts[3].isEmpty() ? null : parts[3];

            return new Account(
                    parts[0],
                    parts[1],
                    parts[2],
                    token,
                    Account.Role.valueOf(parts[4])
            );
        } catch (IllegalArgumentException e) {
            // Catturiamo errori come un Enum non valido (es. un ruolo inesistente)
            throw new DaoException("Dati non validi nella riga CSV: " + line, e);
        }
    }
}
