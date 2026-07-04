-- Inserimento utenti e dati di default
-- Password per tutti: password (bcrypt hash)

-- 1. Inserimento Trainer
INSERT INTO accounts (user_id, email, password_hash, refreshToken, profile_type) VALUES 
('11111111-1111-1111-1111-111111111111', 'trainer@fitplanner.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGFGLn0O51OkmrJ.59pW', NULL, 'TRAINER');

INSERT INTO profiles (user_id, first_name, last_name, contact_email, phone_number, profile_type, invitation_code, trainer_id) VALUES
('11111111-1111-1111-1111-111111111111', 'Super', 'Trainer', 'trainer@fitplanner.com', '1234567890', 'TRAINER', 'TRAINER-1234', NULL);

-- 2. Inserimento Atleti
INSERT INTO accounts (user_id, email, password_hash, refreshToken, profile_type) VALUES 
('22222222-2222-2222-2222-222222222222', 'athlete@fitplanner.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGFGLn0O51OkmrJ.59pW', NULL, 'ATHLETE'),
('33333333-3333-3333-3333-333333333333', 'mario@fitplanner.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGFGLn0O51OkmrJ.59pW', NULL, 'ATHLETE'),
('44444444-4444-4444-4444-444444444444', 'luigi@fitplanner.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGFGLn0O51OkmrJ.59pW', NULL, 'ATHLETE'),
('55555555-5555-5555-5555-555555555555', 'giulia@fitplanner.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGFGLn0O51OkmrJ.59pW', NULL, 'ATHLETE'),
('66666666-6666-6666-6666-666666666666', 'anna@fitplanner.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGFGLn0O51OkmrJ.59pW', NULL, 'ATHLETE');

INSERT INTO profiles (user_id, first_name, last_name, contact_email, phone_number, profile_type, invitation_code, trainer_id) VALUES
('22222222-2222-2222-2222-222222222222', 'John', 'Doe', 'athlete@fitplanner.com', '0000000000', 'ATHLETE', NULL, '11111111-1111-1111-1111-111111111111'),
('33333333-3333-3333-3333-333333333333', 'Mario', 'Rossi', 'mario@fitplanner.com', '0000000000', 'ATHLETE', NULL, '11111111-1111-1111-1111-111111111111'),
('44444444-4444-4444-4444-444444444444', 'Luigi', 'Bianchi', 'luigi@fitplanner.com', '0000000000', 'ATHLETE', NULL, '11111111-1111-1111-1111-111111111111'),
('55555555-5555-5555-5555-555555555555', 'Giulia', 'Verdi', 'giulia@fitplanner.com', '0000000000', 'ATHLETE', NULL, '11111111-1111-1111-1111-111111111111'),
('66666666-6666-6666-6666-666666666666', 'Anna', 'Neri', 'anna@fitplanner.com', '0000000000', 'ATHLETE', NULL, '11111111-1111-1111-1111-111111111111');

-- 4. Inserimento Esercizi (Libreria del Trainer)
INSERT INTO exercise_library (exercise_id, trainer_id, name, execution, muscle_groups) VALUES 
('e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'Squat', 'Esecuzione dello squat con bilanciere', 'Gambe,Glutei'),
('e2222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Panca Piana', 'Distensioni su panca piana con bilanciere', 'Petto,Tricipiti,Spalle'),
('e3333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', 'Trazioni', 'Trazioni alla sbarra presa prona', 'Schiena,Bicipiti'),
('e4444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'Stacco da terra', 'Stacco da terra regolare', 'Schiena,Gambe,Glutei'),
('e5555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', 'Military Press', 'Spinte in alto con bilanciere in piedi', 'Spalle,Tricipiti'),
('e6666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111', 'Leg Press', 'Pressa a 45 gradi', 'Gambe');
