-- 1. Tabella accounts
CREATE TABLE IF NOT EXISTS accounts(
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    refreshToken VARCHAR(255),
    profile_type VARCHAR(50) NOT NULL
);

-- 2. Tabella profiles
CREATE TABLE IF NOT EXISTS profiles(
    user_id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(320),
    phone_number VARCHAR(20),
    invitation_code VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES accounts(user_id) ON DELETE CASCADE
);

-- 3. Tabella coaching
CREATE TABLE IF NOT EXISTS coaching(
    trainer VARCHAR(36) NOT NULL,
    athlete VARCHAR(36) NOT NULL,
    PRIMARY KEY (trainer, athlete),
    FOREIGN KEY (trainer) REFERENCES accounts(user_id) ON DELETE CASCADE,
    FOREIGN KEY (athlete) REFERENCES accounts(user_id) ON DELETE CASCADE
);

-- 4. Tabella exercise_library
CREATE TABLE IF NOT EXISTS exercise_library(
    exercise_id VARCHAR(36) NOT NULL,
    trainer_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    execution TEXT NOT NULL,
    muscle_groups VARCHAR(255) NOT NULL,
    PRIMARY KEY (exercise_id),
    FOREIGN KEY (trainer_id) REFERENCES accounts(user_id) ON DELETE CASCADE
);

-- 5. Tabella session_log
CREATE TABLE IF NOT EXISTS session_log(
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL,
    plan_referenced VARCHAR(36) NOT NULL,
    workout_session_day INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, date),
    FOREIGN KEY (user_id) REFERENCES accounts(user_id) ON DELETE CASCADE
);

-- 6. Tabella exercise_log
CREATE TABLE IF NOT EXISTS exercise_log(
    exercise_log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    exercise_id VARCHAR(36),
    order_index INT NOT NULL,
    exercise_set VARCHAR(255),
    rpe INT,
    name VARCHAR(255) NOT NULL,
    note TEXT,
    FOREIGN KEY (session_id) REFERENCES session_log(session_id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercise_library(exercise_id) ON DELETE SET NULL,
    UNIQUE (session_id, order_index)
    );
