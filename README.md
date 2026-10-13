# Placement Drive Tracker

As the Placement Coordinator of my MCA batch, I used to check in Excel which students were eligible for each company's drive. This Java application keeps students and drives in MySQL and shows the eligible students for any drive in one step.

## Features
- Add, view, update CGPA and delete students
- Add and view placement drives (company, role, minimum CGPA, date)
- Search drives by company name
- Show eligible students for a drive (CGPA >= the drive's minimum), highest CGPA first
- Input checks: CGPA must be 0–10, date must be YYYY-MM-DD, wrong numbers don't crash the program

## Tech
Java 17, JDBC, MySQL

## Project structure
```
placement-drive-tracker/
├── src/com/rajesh/placement/
│   ├── Main.java                  menu and user input
│   ├── model/Student.java         student data
│   ├── model/Drive.java           drive data
│   ├── db/DBConnection.java       JDBC connection to MySQL
│   └── service/PlacementService.java   all SQL queries
├── lib/                           MySQL Connector/J jar goes here
├── schema.sql                     creates the database, tables and sample data
└── run.bat                        compile + run on Windows
```

## How to run
1. Start MySQL (I use XAMPP) and run `schema.sql` in phpMyAdmin → SQL tab.
2. Put `mysql-connector-j-x.x.x.jar` in `lib/` (download from dev.mysql.com/downloads/connector/j, Platform Independent).
3. If your MySQL root user has a password, set it in `DBConnection.java`.
4. Double-click `run.bat`, or in the terminal:
   ```
   javac -d bin -cp "lib\*" src\com\rajesh\placement\*.java src\com\rajesh\placement\model\*.java src\com\rajesh\placement\db\*.java src\com\rajesh\placement\service\*.java
   java -cp "bin;lib\*" com.rajesh.placement.Main
   ```

## Main query
```sql
SELECT s.* FROM student s JOIN drive d ON s.cgpa >= d.min_cgpa
WHERE d.id = ? ORDER BY s.cgpa DESC;
```

## Ideas for next version
- Coordinator login
- Table to record which students applied to which drive
- Web interface
