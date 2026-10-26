-- Dummy data for testing and demo. Names are made up.
-- Run after schema.sql. It clears both tables first, so don't run it on real data.

USE placement_db;

DELETE FROM student;
DELETE FROM drive;
ALTER TABLE student AUTO_INCREMENT = 1;
ALTER TABLE drive AUTO_INCREMENT = 1;

INSERT INTO student (name, branch, cgpa) VALUES
('Amit Kumar',      'MCA', 7.80),
('Priya Sahoo',     'MCA', 6.40),
('Rahul Mishra',    'MCA', 8.50),
('Sneha Patnaik',   'MCA', 9.10),
('Rohit Nayak',     'MCA', 5.90),
('Ananya Das',      'MCA', 7.20),
('Subham Pradhan',  'MCA', 6.85),
('Pooja Mohanty',   'MCA', 8.05),
('Ritesh Behera',   'MCA', 6.10),
('Swati Panda',     'MCA', 7.55),
('Deepak Swain',    'MCA', 5.40),
('Lipsa Rout',      'MCA', 8.75),
('Manas Jena',      'MCA', 6.95),
('Ankita Sethi',    'MCA', 7.35),
('Sourav Dash',     'MCA', 6.60);

INSERT INTO drive (company, role, min_cgpa, drive_date) VALUES
('LTM',                'Graduate Engineer Trainee', 6.0, '2026-10-15'),
('Infosys',            'Systems Engineer',          7.0, '2026-10-20'),
('TCS',                'Ninja Developer',           6.0, '2026-11-12'),
('Wipro',              'Project Engineer',          6.5, '2026-11-18'),
('Accenture',          'Associate Software Engineer', 6.5, '2026-11-25'),
('Capgemini',          'Analyst',                   6.0, '2026-12-02'),
('Cognizant',          'GenC Developer',            7.5, '2026-12-09'),
('Mindfire Solutions', 'Java Trainee',              8.0, '2027-01-14');
