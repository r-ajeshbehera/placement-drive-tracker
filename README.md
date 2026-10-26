# Placement Drive Tracker

As the Placement Coordinator of my MCA batch, I used to check in Excel which students were eligible for each company's drive. This Java application keeps students and drives in MySQL and shows the eligible students for any drive in one step.

## Features
- Add, view, update CGPA and delete students
- Add and view placement drives (company, role, minimum CGPA, date)
- Search drives by company name
- Show eligible students for a drive (CGPA >= the drive's minimum), highest CGPA first
- Input checks: CGPA must be 0–10, date must be YYYY-MM-DD, wrong numbers don't crash the program

## Tech
Java 17, JDBC, MySQL. Web version: plain Java (JDK built-in `HttpServer`, no framework) + HTML, CSS and JavaScript.

## Two ways to use it
| Version | Start with | What you get |
|---|---|---|
| Console | `run.bat` | Menu in the terminal (the original version) |
| Web | `run-web.bat` | Dashboard in the browser at http://localhost:8085 |

Both use the same `PlacementService` and the same MySQL database.

## Project structure
```
placement-drive-tracker/
├── src/com/rajesh/placement/
│   ├── Main.java                  menu and user input
│   ├── model/Student.java         student data
│   ├── model/Drive.java           drive data
│   ├── db/DBConnection.java       JDBC connection to MySQL
│   ├── service/PlacementService.java   all SQL queries
│   └── web/
│       ├── WebServer.java         REST API + serves the web pages
│       ├── HttpUtil.java          read requests, send JSON / files
│       └── Json.java              Student/Drive -> JSON text
├── web/                           frontend: index.html, style.css, app.js
├── lib/                           MySQL Connector/J jar goes here
├── schema.sql                     creates the database and tables
├── sample-data.sql                15 dummy students + 8 drives
├── run.bat                        console version
└── run-web.bat                    web version
```

## How to run
1. Start MySQL (I use XAMPP) and run `schema.sql`, then `sample-data.sql`, in phpMyAdmin → SQL tab. (`sample-data.sql` clears both tables first.)
2. Put `mysql-connector-j-x.x.x.jar` in `lib/` (download from dev.mysql.com/downloads/connector/j, Platform Independent).
3. If your MySQL root user has a password, set it in `DBConnection.java`.
4. Web version: double-click `run-web.bat`. The browser opens http://localhost:8085.
5. Console version: double-click `run.bat`, or in the terminal:
   ```
   javac -d bin -cp "lib\*" src\com\rajesh\placement\*.java src\com\rajesh\placement\model\*.java src\com\rajesh\placement\db\*.java src\com\rajesh\placement\service\*.java
   java -cp "bin;lib\*" com.rajesh.placement.Main
   ```

## Web version
Pages: **Dashboard** (counts + upcoming drives with eligible count), **Students** (add, filter, edit CGPA, delete), **Drives** (add, search, delete), **Eligibility** (pick a drive, see eligible students ranked by CGPA).

REST API (all JSON):
| Method | Path | Does |
|---|---|---|
| GET | `/api/stats` | students, drives, upcoming drives, average CGPA |
| GET | `/api/students` | all students, highest CGPA first |
| POST | `/api/students` | add (name, branch, cgpa) |
| POST | `/api/students/update` | change CGPA (id, cgpa) |
| POST | `/api/students/delete` | delete (id) |
| GET | `/api/drives?q=` | all drives, or search by company |
| POST | `/api/drives` | add (company, role, minCgpa, date) |
| POST | `/api/drives/delete` | delete (id) |
| GET | `/api/eligible?driveId=` | drive + eligible students |

The server checks every input again (CGPA 0–10, date YYYY-MM-DD, required fields), so the rules are the same as the console version. The frontend uses `textContent`, so names can't inject HTML.

## Main query
```sql
SELECT s.* FROM student s JOIN drive d ON s.cgpa >= d.min_cgpa
WHERE d.id = ? ORDER BY s.cgpa DESC;
```

## Ideas for next version
- Coordinator login
- Table to record which students applied to which drive
- Export eligible list to CSV
