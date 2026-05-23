package com.example.fitplannerclient.controller.plan.engine;

public interface WorkoutEngine {
    void play();
    void pause();
    void stop();

    void skipPrevious();
    void skipNext();
}
