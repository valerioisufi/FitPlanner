package com.example.fitplannerserver.util;

import java.security.SecureRandom;

public class InvitationCodeGenerator {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 8;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random human-readable code formatted as XXXX-XXXX
     */
    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(randomIndex));
        }

        sb.insert(CODE_LENGTH / 2, '-');

        return sb.toString();
    }
}