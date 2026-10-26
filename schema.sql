-- Run this once in phpMyAdmin (SQL tab), MySQL Workbench or the MySQL command line.
-- It creates the database and tables. Then run sample-data.sql for dummy data.

CREATE DATABASE IF NOT EXISTS placement_db;
USE placement_db;

-- One row = one student
CREATE TABLE IF NOT EXISTS student (
    id     INT PRIMARY KEY AUTO_INCREMENT,
    name   VARCHAR(100) NOT NULL,
    branch VARCHAR(50)  NOT NULL,
    cgpa   DOUBLE       NOT NULL
);

-- One row = one company visiting for placement
CREATE TABLE IF NOT EXISTS drive (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    company    VARCHAR(100) NOT NULL,
    role       VARCHAR(100) NOT NULL,
    min_cgpa   DOUBLE       NOT NULL,
    drive_date DATE         NOT NULL
);
