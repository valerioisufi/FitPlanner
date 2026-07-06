package com.example.fitplannerclient.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?\\d{8,15}$");
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,32}$");

    // Matches any Unicode letter, spaces, dots, apostrophes, and hyphens.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s.'-]+$");

    private static final String MSG_FIELD_PREFIX = "Il campo ";
    private static final String MSG_REQUIRED_SUFFIX = " è obbligatorio";
    private static final String MSG_EXCEEDS_LENGTH = " non può superare i ";
    private static final String MSG_CHARS_SUFFIX = " caratteri";

    private ValidationUtils() {}

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "L'email è obbligatoria";
        if (email.length() > 320) return "L'email è troppo lunga";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return "Formato email non valido";
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) return "La password è obbligatoria";
        if (!PASSWORD_REGEX.matcher(password).matches()) return "Min 8 car., 1 maiusc, 1 minusc, 1 num";
        return null;
    }

    public static String validatePasswordMatch(String password, String confirm) {
        if (confirm == null || confirm.trim().isEmpty()) return "Conferma la password";
        if (!confirm.equals(password)) return "Le password non coincidono";
        return null;
    }

    public static String validateName(String name, String fieldName, int maxLength) {
        if (name == null || name.trim().isEmpty()) return MSG_FIELD_PREFIX + fieldName + MSG_REQUIRED_SUFFIX;
        if (name.length() > maxLength) return fieldName + MSG_EXCEEDS_LENGTH + maxLength + MSG_CHARS_SUFFIX;
        if (!NAME_PATTERN.matcher(name.trim()).matches()) return fieldName + " contiene caratteri non validi";
        return null;
    }

    public static String validateRequired(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) return MSG_FIELD_PREFIX + fieldName + MSG_REQUIRED_SUFFIX;
        if (value.length() > maxLength) return fieldName + MSG_EXCEEDS_LENGTH + maxLength + MSG_CHARS_SUFFIX;
        return null;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // optional field
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) return "Formato telefono non valido";
        return null;
    }

    public static String validateIntegerInRange(String value, String fieldName, int min, int max) {
        if (value == null || value.trim().isEmpty())
            return MSG_FIELD_PREFIX + fieldName + MSG_REQUIRED_SUFFIX;

        int parsed;
        try {
            parsed = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fieldName + " deve essere un numero intero";
        }

        if (parsed < min || parsed > max)
            return fieldName + " deve essere tra " + min + " e " + max;

        return null;
    }

    public static String validateDecimalInRange(String value, String fieldName, double min, double max) {
        if (value == null || value.trim().isEmpty())
            return MSG_FIELD_PREFIX + fieldName + MSG_REQUIRED_SUFFIX;

        double parsed;
        try {
            parsed = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fieldName + " deve essere un numero valido";
        }

        if (parsed < min || parsed > max)
            return fieldName + " deve essere tra " + formatLimit(min) + " e " + formatLimit(max);

        return null;
    }

    public static String validateOptionalMaxLength(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return null; // optional field
        }

        if (value.length() > maxLength)
            return fieldName + MSG_EXCEEDS_LENGTH + maxLength + MSG_CHARS_SUFFIX;

        return null;
    }

    private static String formatLimit(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}