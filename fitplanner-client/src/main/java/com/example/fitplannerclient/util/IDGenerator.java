package com.example.fitplannerclient.util;

import java.util.UUID;

public final class IDGenerator {

    private IDGenerator(){};

    public static String generateUUID(){
        return UUID.randomUUID().toString();
    }
}
