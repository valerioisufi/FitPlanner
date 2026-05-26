package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.SessionLogDao;

public class DatabaseSessionLogDao implements SessionLogDao {
    /** String sql= """CREATE TABLE IF NOT EXIST sessionLog(
     *                user_id VARCHAR(255) NOT NULL,
     *                notes TEXT,
     *                status VARCHAR(11) NOT NULL,
     *                date TIMESTAMP DEFAULT NOW,
     *                plan_referenced VARCHAR(36) NOT NULL,
     *                exercise_log_id VARCHAR(36) PRIMARY KEY,
     *                FOREIGN KEY (exercise_log_id) REFERENCES exercise_log(exercise_log_id) ON DELETE CASCADE,
     *                FOREIGN KEY (user_id) REFERENCES accounts(user_id) ON DELETE CASCADE);
     *              """;
     *
     *   String sql= """CREATE TABLE IF NOT EXIST exercise_log(
     *                  name VARCHAR(50) NOT NULL,
     *                  exercise_id VARCHAR(36) PRIMARY KEY,
     *                  exercise_set VARCHAR(255),
     *                  rpe INTEGER,
     *                  notes TEXT,
     *                  FOREIGN KEY (exercise_id) REFERENCES exercise_library(exercise_id));
     *
     */
}
