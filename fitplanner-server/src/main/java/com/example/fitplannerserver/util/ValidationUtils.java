package com.example.fitplannerserver.util;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{8,15}$");
    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s.'-]+$");

    private ValidationUtils() {}

    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isLengthBetween(String str, int min, int max) {
        if (str == null) return false;
        int length = str.trim().length();
        return length >= min && length <= max;
    }

    public static boolean isLengthAtMost(String str, int max) {
        if (str == null) return false;
        return str.trim().length() <= max;
    }

    public static boolean isValidUuid(String uuid) {
        if (isNullOrBlank(uuid)) return false;
        return UUID_PATTERN.matcher(uuid).matches();
    }

    public static boolean isValidName(String name) {
        if (isNullOrBlank(name)) return false;
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrBlank(email)) return false;
        if (!isLengthAtMost(email, 320)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (isNullOrBlank(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        try {
            mapper.readTree(jsonString);
            return true;
        } catch (JacksonException e) {
            // il JSON non è valido
            return false;
        }

    }

}