package com.example.fitplannerclient.ui.cli.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class InputReader {
    private final Scanner scanner;
    private final OutputPrinter printer;

    public InputReader(Scanner scanner, OutputPrinter printer) {
        this.scanner = scanner;
        this.printer = printer;
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readStringAndValidate(String prompt, UnaryOperator<String> validator) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        String msg = validator.apply(input);

        if (msg != null) {
            printer.printError(msg);
            return readStringAndValidate(prompt, validator);
        }
        return input;
    }

    public String readStringAndValidate(String prompt, UnaryOperator<String> validator, String defaultInput) {
        System.out.print(prompt + " [" + defaultInput + "]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            input = defaultInput;
        }

        String msg = validator.apply(input);

        if (msg != null) {
            printer.printError(msg);
            return readStringAndValidate(prompt, validator, defaultInput);
        }

        return input;
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printer.printError("Input non valido. Inserisci un numero intero.");
            }
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            } else {
                printer.printError("Il valore deve essere compreso tra " + min + " e " + max + ".");
            }
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            // Allow both comma and dot for decimals
            input = input.replace(",", ".");
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                printer.printError("Input non valido. Inserisci un numero decimale valido.");
            }
        }
    }

    public void waitForEnter() {
        System.out.println("Premi invio per continuare...");
        scanner.nextLine();
    }

    /**
     * Mostra un menu numerato degli items (più una voce di annullamento)
     * e restituisce l'elemento scelto, o vuoto se l'utente annulla.
     */
    public <T> Optional<T> selectFrom(String title, List<T> items, Function<T, String> label) {
        return selectFrom(title, items, label, "Annulla");
    }

    public <T> Optional<T> selectFrom(String title, List<T> items, Function<T, String> label, String cancelLabel) {
        List<String> options = new ArrayList<>();
        for (T item : items) {
            options.add(label.apply(item));
        }
        options.add(cancelLabel);

        printer.printMenu(title, options);
        int choice = readInt("Scelta: ", 1, options.size());

        if (choice == options.size()) {
            return Optional.empty();
        }
        return Optional.of(items.get(choice - 1));
    }
}
