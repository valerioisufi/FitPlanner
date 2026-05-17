package com.example.fitplannerserver.model.plan;

public class WorkoutSession {
    private String title;
    private String jsonContent;

    private int day;

    public WorkoutSession(String title, String jsonContent, int day) {

        this.title = title;
        this.jsonContent = jsonContent;

        this.day = day;
    }

    public WorkoutSession(WorkoutSession old){
        this.title = old.title;
        this.jsonContent = old.jsonContent;
        this.day = old.day;
    }

    public String getTitle() {
        return title;
    }

    public String getContent(){
        return jsonContent;
    }

    public int getDay() {
        return day;
    }

}