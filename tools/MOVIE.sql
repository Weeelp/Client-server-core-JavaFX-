DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS users;
DROP TYPE IF EXISTS movie_genre;
DROP TYPE IF EXISTS person_eye_color;
DROP TYPE IF EXISTS person_hair_color;
DROP TYPE IF EXISTS person_nationality;

CREATE TYPE movie_genre AS ENUM (
    'WESTERN', 'COMEDY', 'TRAGEDY', 'SCIENCE_FICTION'
);

CREATE TYPE person_eye_color AS ENUM (
    'BLACK', 'BLUE', 'WHITE', 'BROWN'
);

CREATE TYPE person_hair_color AS ENUM (
    'GREEN', 'RED', 'ORANGE', 'WHITE'
);

CREATE TYPE person_nationality AS ENUM (
    'RUSSIA', 'UNITED_KINGDOM', 'GERMANY', 'ITALY', 'JAPAN', 'AMERICA'
);

CREATE TABLE IF NOT EXISTS person (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL CHECK (name <> ''),
    height INT NOT NULL CHECK (height > 0),
    eye_color person_eye_color,
    hair_color person_hair_color,
    nationality person_nationality
);

CREATE TABLE IF NOT EXISTS movies (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL CHECK (name <> ''),
    x BIGINT NOT NULL,
    y FLOAT NOT NULL,
    creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    oscars_count INT NOT NULL CHECK (oscars_count > 0),
    total_box_office DOUBLE PRECISION CHECK (total_box_office > 0),
    usa_box_office BIGINT CHECK (usa_box_office > 0),
    genre movie_genre NOT NULL,
    screenwriter_id INT REFERENCES person(id) ON DELETE SET NULL,
    owner_login TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login TEXT NOT NULL UNIQUE CHECK (login <> ''),
    password_hash TEXT NOT NULL
);

INSERT INTO users (login, password_hash) VALUES ('a', '1234');

INSERT INTO person (name, height, eye_color, hair_color, nationality) VALUES
('Reader', 178, 'BLACK', 'WHITE', 'JAPAN'),     -- id 1
('Epstein', 178, 'BLACK', 'WHITE', 'AMERICA'), -- id 2
('wefg', 2345, 'BLACK', 'ORANGE', 'GERMANY'),  -- id 3
('3', 3, 'BLUE', 'RED', 'ITALY'),              -- id 4
('3', 3, 'BLACK', 'GREEN', 'RUSSIA'),          -- id 5
('ed', 4, 'BLUE', 'RED', 'GERMANY'),           -- id 6
('4', 4, 'BLUE', 'RED', 'RUSSIA'),             -- id 7
('1', 1, 'BLUE', 'RED', 'RUSSIA'),             -- id 8
('1', 1, 'BLUE', 'RED', 'ITALY'),              -- id 9
('1', 1, 'BLUE', 'RED', 'RUSSIA');             -- id 10

INSERT INTO movies (name, x, y, creation_date, oscars_count, total_box_office, usa_box_office, genre, screenwriter_id, owner_login) VALUES
('Omnistian reader', 12, 51.0, '2026-02-16', 100, 10002222, 1000077550000, 'TRAGEDY', 1, 'a'),
('Epstein Files', 12, 51.0, '2026-02-16', 10000, 10002222, 100007755000012, 'TRAGEDY', 2, 'a'),
('as', 34567, 345.0, '2026-02-16', 23456, 45.0, 34567, 'COMEDY', 3, 'a'),
('a', 2, 2.0, '2026-02-16', 2, 2.0, 2, 'WESTERN', 4, 'a'),
('d', 3, 3.0, '2026-02-16', 3, 3.0, 3, 'WESTERN', 5, 'a'),
('9', 2, 2.0, '2026-02-25', 3, 2.0, 2, 'WESTERN', 6, 'a'),
('w', 3, 3.0, '2026-02-25', 3, 3.0, 3, 'WESTERN', 7, 'a'),
('1', 1, 1.0, '2026-03-07', 1, 1.0, 1, 'COMEDY', 8, 'a'),
('1', 1, 1.0, '2026-03-24', 1, 1.0, 1, 'WESTERN', 9, 'a'),
('1', 1, 1.0, '2026-03-24', 1, 1.0, 1, 'COMEDY', 10, 'a');
