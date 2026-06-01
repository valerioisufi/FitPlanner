package com.example.fitplannerclient.ui.cli;

import java.util.Scanner;
import java.util.function.Function;

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

    public String readStringAndValidate(String prompt, Function<String, String> validator) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        String msg = validator.apply(input);

        if (msg != null) {
            printer.printError(msg);
            return readStringAndValidate(prompt, validator);
        }
        return input;
    }

    public String readStringAndValidate(String prompt, Function<String, String> validator, String defaultInput) {
        System.out.print(prompt + " [" + defaultInput + "]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            input = defaultInput;
        }

        String msg = validator.apply(input);

        if (msg != null) {
            printer.printError(msg);
            return readStringAndValidate(prompt, validator);
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
}
