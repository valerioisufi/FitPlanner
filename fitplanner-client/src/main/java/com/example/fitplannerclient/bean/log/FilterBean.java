package com.example.fitplannerclient.bean.log;

import java.util.Map;

public record FilterBean(
        long startDate,
        long endDate,
        Map<String, String> exercises // exerciseId -> exerciseName
) {
}
