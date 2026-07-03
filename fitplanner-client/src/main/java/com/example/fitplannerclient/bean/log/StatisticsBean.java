package com.example.fitplannerclient.bean.log;

import java.util.List;

public record StatisticsBean(
        long startDate,
        long endDate,
        String exerciseId,
        String exerciseName,
        List<SessionStatisticsBean> sessionStatisticsList
) {
    public record SessionStatisticsBean(long date, double totalVolume) {}
}