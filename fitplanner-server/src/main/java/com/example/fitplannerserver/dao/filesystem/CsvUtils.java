package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.model.Account;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CsvUtils {

    public static final String CSV_DELIMITER = ";";

    private CsvUtils() {}

    public static void initializeFile(File targetFile, String header) {
        if (!targetFile.exists()) {
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
                writer.write(header);
                writer.newLine();

            } catch (IOException e) {
                LoggerFactory.getLogger(FileSystemAccountDao.class).error("Errore durante l'inizializzazione del file CSV", e);
                throw new SystemException("Inizializzazione DAO fallita per " + targetFile.getName());
            }
        }

    }

    public static String convertNullToEmptyString(String value) {
        // convert null to empty String
        return value == null ? "" : value;
    }

    public static String convertEmptyStringToNull(String value) {
        // convert empty String to null
        return Objects.equals(value, "") ? null : value;
    }

    public static String[] csvSplit(String line, int expectedColumns) {
        String[] parts = line.split(CSV_DELIMITER, -1);
        if (parts.length != expectedColumns) {
            throw new IllegalArgumentException("Expected " + expectedColumns + " columns but got " + parts.length);
        }
        return parts;
    }

    public static List<String[]> search(File file, int expectedColumns, Predicate<String[]> filter, int limit) throws IOException {

        try (var in = new BufferedReader(new FileReader(file))) {
            List<String[]> results = new ArrayList<>();

            in.readLine(); // leggo e scarto l'intestazione

            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (limit != -1 && results.size() >= limit) {
                    break;
                }

                String[] parts = csvSplit(line, expectedColumns);

                if (filter.test(parts)) {
                    results.add(parts);
                }
            }

            return results;

        }

    }

    public static boolean update(File file, int expectedColumns, Predicate<String[]> filter, String newRow) throws IOException {
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;

                    writer.write(line);
                    writer.newLine();
                    continue;
                }
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

            if (!updated) {
                writer.write(newRow);
                writer.newLine();
            }
        }

        if (file.delete()) {
            if (!tempFile.renameTo(file)) {
                throw new IOException("Impossibile rinominare il file temporaneo in quello originale.");
            }
        } else {
            throw new IOException("Impossibile eliminare il file originale per l'aggiornamento.");
        }

        return updated;
    }


    public static void append(File file, String newRow) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(newRow);
            writer.newLine();
        }
    }

    public static boolean delete(File file, int expectedColumns, Predicate<String[]> filter) throws IOException {
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        boolean isDeleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;

                    writer.write(line);
                    writer.newLine();
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                if (filter.test(csvSplit(line, expectedColumns))) {
                    isDeleted = true;
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        if (isDeleted) {
            if (file.delete()) {
                if (!tempFile.renameTo(file)) {
                    throw new IOException("Impossibile rinominare il file temporaneo in quello originale.");
                }
            } else {
                throw new IOException("Impossibile eliminare il file originale per l'aggiornamento.");
            }
        } else {
            tempFile.delete();
        }

        return isDeleted;

    }


}
