package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.exception.DaoException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.CSV_DELIMITER;
import static com.example.fitplannerserver.dao.filesystem.CsvUtils.initializeFile;

public class FileSystemCoachingDao implements CoachingDao {
    private static final String CSV_HEADER = "ATHLETE;TRAINER";
    private static final String NULL_ATHLETE_ID_MSG ="athlete id cannot be null";
    private static final String NULL_TRAINER_ID_MSG ="trainer id cannot be null";
    private static final int EXPECTED_COLUMNS = 2;
    private final Path path;
    private final ReadWriteLock lock= new ReentrantReadWriteLock();


    public FileSystemCoachingDao(Path path){
        this.path= Objects.requireNonNull(path, "path cannot be null");
        initializeFile(this.path, CSV_HEADER);
    }

    @Override
    public void linkAthleteToTrainer(String athleteUuid, String trainerUuid) throws DaoException {
        Objects.requireNonNull(athleteUuid, NULL_ATHLETE_ID_MSG);
        Objects.requireNonNull(trainerUuid, NULL_TRAINER_ID_MSG);

        lock.writeLock().lock();
        try{
            boolean linked= !CsvUtils.search(path, EXPECTED_COLUMNS, parts -> parts[0].equals(athleteUuid), 1).isEmpty();
            if(linked){
                throw new DaoException("L'atleta è già collegato a un trainer");
            }
            CsvUtils.append(path, String.join(CSV_DELIMITER, athleteUuid, trainerUuid));
        }catch (IOException e){
            throw new DaoException("Errore durante il collegamento tra il trainer e l'atleta", e);
        }finally{
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unlink(String athleteId, String trainerId) throws DaoException {
        Objects.requireNonNull(athleteId, NULL_ATHLETE_ID_MSG);
        Objects.requireNonNull(trainerId, NULL_TRAINER_ID_MSG);
        lock.writeLock().lock();
        try{
            CsvUtils.delete(path, EXPECTED_COLUMNS, parts -> parts[0].equals(athleteId) && parts[1].equals(trainerId));
        }catch (IOException e){
            throw new DaoException("Errore durante l'eliminazione del collegamento tra il trainer e l'atleta", e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isClientOf(String trainerId, String athleteId) throws DaoException {
        Objects.requireNonNull(athleteId, NULL_ATHLETE_ID_MSG);
        Objects.requireNonNull(trainerId, NULL_TRAINER_ID_MSG);
        lock.readLock().lock();
        try{
            return !CsvUtils.search(path, EXPECTED_COLUMNS, parts -> parts[0].equals(athleteId) && parts[1].equals(trainerId), 1).isEmpty();
        }catch (IOException e){
            throw new DaoException("Errore durante la ricerca del collegamento tra il trainer e l'atleta", e);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<String> findAthleteIdsByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, NULL_TRAINER_ID_MSG);
        lock.readLock().lock();
        try{
            return CsvUtils.search(path, EXPECTED_COLUMNS, parts -> parts[1].equals(trainerId), -1).stream().map(parts -> parts[0]).toList();
        }catch (IOException e){
            throw new DaoException("Errore durante la ricerca degli atleti associati al trainer", e);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<String> findTrainerIdByAthleteId(String athleteId) throws DaoException {
        Objects.requireNonNull(athleteId, NULL_ATHLETE_ID_MSG);
        lock.readLock().lock();
        try{
            return CsvUtils.search(path, EXPECTED_COLUMNS, parts -> parts[0].equals(athleteId), 1).stream().findFirst().map(parts -> parts[1]);
        }catch (IOException e){
            throw new DaoException("Errore durante la ricerca del trainer associato all'atleta", e);
        }finally {
            lock.readLock().unlock();
        }
    }
}
