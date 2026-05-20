package com.example.fitplannercommon;

public class WorkoutSessionDTO {
    private String name;
    private String content;

    private int day;

    public WorkoutSessionDTO(){}

    public WorkoutSessionDTO(String name, String content, int day) {
        this.name = name;
        this.content = content;

        this.day = day;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
}

