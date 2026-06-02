package com.example.fitplannerclient.ui.cli;

import java.util.List;

public class OutputPrinter {

    public void printHeader(String title) {
        System.out.println("\n========================================");
        System.out.println("  " + title.toUpperCase());
        System.out.println("========================================");
    }

    public void printMenu(String title, List<String> options) {
        if (title != null && !title.isEmpty()) {
            System.out.println("\n--- " + title + " ---");
        } else {
            System.out.println();
        }
        
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
    }

    public void printError(String message) {
        System.out.println("[ERRORE] " + message);
    }

    public void printException(String message, Throwable ex) {
        String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        System.out.println("[ERRORE] " + message + errorMsg);
    }

    public void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    public void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * Stampa i dati in formato tabellare.
     */
    public void printTable(String[] headers, String[][] data) {
        if (headers == null || headers.length == 0) return;
        int[] colWidths = calculateColumnWidths(headers, data);
        String format = buildFormatString(colWidths);
        printHeaderAndSeparator(headers, colWidths, format);
        if (data != null) {
            for (String[] row : data) {
                System.out.printf(format, (Object[]) row);
            }
        }
    }

    private int[] calculateColumnWidths(String[] headers, String[][] data) {
        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }
        if (data != null) {
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    if (row[i] != null && row[i].length() > colWidths[i]) {
                        colWidths[i] = row[i].length();
                    }
                }
            }
        }
        return colWidths;
    }

    private String buildFormatString(int[] colWidths) {
        StringBuilder formatBuilder = new StringBuilder();
        for (int width : colWidths) {
            formatBuilder.append("%-").append(width + 2).append("s");
        }
        formatBuilder.append("%n");
        return formatBuilder.toString();
    }

    private void printHeaderAndSeparator(String[] headers, int[] colWidths, String format) {
        System.out.printf(format, (Object[]) headers);
        StringBuilder separator = new StringBuilder();
        for (int width : colWidths) {
            separator.append("-".repeat(width + 2));
        }
        System.out.println(separator.toString());
    }
}
