-- Run this once in MySQL Workbench or the MySQL command line

CREATE DATABASE IF NOT EXISTS placement_db;
USE placement_db;

-- One row = one student
CREATE TABLE student (
    id     INT PRIMARY KEY AUTO_INCREMENT,
    name   VARCHAR(100) NOT NULL,
    branch VARCHAR(50)  NOT NULL,
    cgpa   DOUBLE       NOT NULL
);

-- One row = one company visiting for placement
CREATE TABLE drive (
    id        INT PRIMARY KEY AUTO_INCREMENT,
    company   VARCHAR(100) NOT NULL,
    role      VARCHAR(100) NOT NULL,
    min_cgpa  DOUBLE       NOT NULL,
    drive_date DATE        NOT NULL
);

-- Some sample data to test with
INSERT INTO student (name, branch, cgpa) VALUES
('Amit',  'MCA', 7.8),
('Priya', 'MCA', 6.4),
('Rahul', 'MCA', 8.5);

INSERT INTO drive (company, role, min_cgpa, drive_date) VALUES
('LTM',     'Graduate Engineer Trainee', 6.0, '2026-10-15'),
('Infosys', 'Systems Engineer',          7.0, '2026-10-20');
