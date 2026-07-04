package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.exception.SystemException;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CsvUtils {

    public static final String CSV_DELIMITER = ";";

    private CsvUtils() {}

    public static void initializeFile(Path targetPath, String header) {
        if (Files.notExists(targetPath)) {
            Path parent = targetPath.getParent();
            if (parent != null && Files.notExists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    LoggerFactory.getLogger(CsvUtils.class).error("Errore creazione directory per file CSV", e);
                    throw new SystemException("Creazione cartelle fallita per " + targetPath.getFileName());
                }
            }

            // StandardOpenOption.CREATE assicura che il file venga creato, con codifica UTF-8 di default
            try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardOpenOption.CREATE)) {
                writer.write(header);
                writer.newLine();
            } catch (IOException e) {
                LoggerFactory.getLogger(CsvUtils.class).error("Errore durante l'inizializzazione del file CSV", e);
                throw new SystemException("Inizializzazione DAO fallita per " + targetPath.getFileName());
            }
        }
    }

    public static String convertNullToEmptyString(String value) {
        return value == null ? "" : value;
    }

    public static String convertEmptyStringToNull(String value) {
        return Objects.equals(value, "") ? null : value;
    }

    public static String[] csvSplit(String line, int expectedColumns) {
        String[] parts = line.split(CSV_DELIMITER, -1);
        if (parts.length != expectedColumns) {
            throw new IllegalArgumentException("Attese " + expectedColumns + " colonne, ma trovate " + parts.length);
        }
        return parts;
    }

    public static List<String[]> search(Path file, int expectedColumns, Predicate<String[]> filter, int limit) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file)) {
            List<String[]> results = new ArrayList<>();

            String header = in.readLine();
            if (header == null) {
                return results; // scarto l'intestazione
            }

            String line;
            while ((limit == -1 || results.size() < limit) && (line = in.readLine()) != null) {

                if (!line.trim().isEmpty()) {
                    String[] parts = csvSplit(line, expectedColumns);
                    if (filter.test(parts)) {
                        results.add(parts);
                    }
                }
            }

            return results;
        }
    }

    public static boolean update(Path file, int expectedColumns, Predicate<String[]> filter, String newRow) throws IOException {
        // resolveSibling crea un file temporaneo nella stessa esatta cartella del file originale
        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
        boolean updated = false;

        try (BufferedReader reader = Files.newBufferedReader(file);
             BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardOpenOption.CREATE)) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    writer.write(line);
                    writer.newLine();
                } else {
                    if (line.trim().isEmpty()) continue;
    
                    String[] parts = csvSplit(line, expectedColumns);
                    if (!updated && filter.test(parts)) {
                        writer.write(newRow);
                        updated = true;
                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                }
            }

            if (!updated) {
                writer.write(newRow);
                writer.newLine();
            }
        }

        // Operazione atomica: sposta il temp sul file originale sovrascrivendolo infallibilmente
        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);

        return updated;
    }

    public static void append(Path file, String newRow) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(newRow);
            writer.newLine();
        }
    }

    public static boolean delete(Path file, int expectedColumns, Predicate<String[]> filter) throws IOException {
        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
        boolean isDeleted = false;

        try (BufferedReader reader = Files.newBufferedReader(file);
             BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardOpenOption.CREATE)) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    writer.write(line);
                    writer.newLine();
                } else {
                    if (line.trim().isEmpty()) continue;
    
                    if (filter.test(csvSplit(line, expectedColumns))) {
                        isDeleted = true;
                    } else {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }
        }

        if (isDeleted) {
            // Se abbiamo eliminato qualcosa, sostituiamo il file originale
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
        } else {
            // Se non c'era nulla da eliminare, distruggiamo semplicemente il file temporaneo
            Files.deleteIfExists(tempFile);
        }

        return isDeleted;
    }
}