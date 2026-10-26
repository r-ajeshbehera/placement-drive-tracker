package com.rajesh.placement.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import com.rajesh.placement.model.Drive;
import com.rajesh.placement.model.Student;
import com.rajesh.placement.service.PlacementService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/*
 * Web version of the Placement Drive Tracker.
 * Backend: plain Java (JDK HttpServer) + the same PlacementService (JDBC + MySQL).
 * Frontend: HTML, CSS and JavaScript in the web/ folder.
 *
 * REST API
 *   GET  /api/stats                      counts for the dashboard
 *   GET  /api/students                   all students, highest CGPA first
 *   POST /api/students                   name, branch, cgpa
 *   POST /api/students/update            id, cgpa
 *   POST /api/students/delete            id
 *   GET  /api/drives?q=infosys           all drives, or search by company
 *   POST /api/drives                     company, role, minCgpa, date
 *   POST /api/drives/delete              id
 *   GET  /api/eligible?driveId=1         drive + eligible students (SQL JOIN)
 */
public class WebServer {

    private static final PlacementService service = new PlacementService();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8085;
        Path webRoot = HttpUtil.webRoot();

        // if the port is busy (another app is using it), try the next ones
        HttpServer server = null;
        for (int tries = 0; tries < 10 && server == null; tries++) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (java.net.BindException e) {
                System.out.println("Port " + port + " is busy, trying " + (port + 1) + "...");
                port++;
            }
        }
        if (server == null) {
            System.out.println("Could not find a free port. Close other servers and try again.");
            return;
        }
        server.createContext("/api/", WebServer::handleApi);
        server.createContext("/", ex -> HttpUtil.sendStaticFile(ex, webRoot));
        server.start();

        System.out.println("Placement Drive Tracker is running.");
        System.out.println("Open http://localhost:" + port + " in your browser. Press Ctrl+C to stop.");
        openBrowser("http://localhost:" + port);
    }

    // opens the default browser on Windows/Mac/Linux; if it can't, the user opens the link
    private static void openBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            }
        } catch (Exception e) {
            // not important: the link is printed above
        }
    }

    private static void handleApi(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();
        try {
            if (method.equals("GET") && path.equals("/api/stats")) {
                stats(ex);
            } else if (method.equals("GET") && path.equals("/api/students")) {
                HttpUtil.sendJson(ex, 200, Json.students(service.getAllStudents()));
            } else if (method.equals("POST") && path.equals("/api/students")) {
                addStudent(ex);
            } else if (method.equals("POST") && path.equals("/api/students/update")) {
                updateCgpa(ex);
            } else if (method.equals("POST") && path.equals("/api/students/delete")) {
                Map<String, String> f = HttpUtil.body(ex);
                boolean done = service.deleteStudent(readInt(f, "id"));
                reply(ex, done, "Student deleted.", "No student with that id.");
            } else if (method.equals("GET") && path.equals("/api/drives")) {
                String q = HttpUtil.query(ex).getOrDefault("q", "");
                List<Drive> drives = q.isEmpty() ? service.getAllDrives() : service.searchDrives(q);
                HttpUtil.sendJson(ex, 200, Json.drives(drives));
            } else if (method.equals("POST") && path.equals("/api/drives")) {
                addDrive(ex);
            } else if (method.equals("POST") && path.equals("/api/drives/delete")) {
                Map<String, String> f = HttpUtil.body(ex);
                boolean done = service.deleteDrive(readInt(f, "id"));
                reply(ex, done, "Drive deleted.", "No drive with that id.");
            } else if (method.equals("GET") && path.equals("/api/eligible")) {
                eligible(ex);
            } else {
                HttpUtil.sendError(ex, 404, "Unknown API: " + method + " " + path);
            }
        } catch (IllegalArgumentException e) {
            HttpUtil.sendError(ex, 400, e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            HttpUtil.sendError(ex, 500, "Database error. Is MySQL running? " + e.getMessage());
        }
    }

    private static void stats(HttpExchange ex) throws IOException, SQLException {
        List<Student> students = service.getAllStudents();
        List<Drive> drives = service.getAllDrives();
        double total = 0;
        for (Student s : students) {
            total += s.getCgpa();
        }
        double avg = students.isEmpty() ? 0 : total / students.size();
        LocalDate today = LocalDate.now();
        int upcoming = 0;
        for (Drive d : drives) {
            if (!LocalDate.parse(d.getDriveDate()).isBefore(today)) {
                upcoming++;
            }
        }
        String json = "{\"students\":" + students.size()
                + ",\"drives\":" + drives.size()
                + ",\"upcoming\":" + upcoming
                + ",\"avgCgpa\":" + String.format("%.2f", avg) + "}";
        HttpUtil.sendJson(ex, 200, json);
    }

    private static void addStudent(HttpExchange ex) throws IOException, SQLException {
        Map<String, String> f = HttpUtil.body(ex);
        String name = readText(f, "name");
        String branch = readText(f, "branch");
        double cgpa = readCgpa(f, "cgpa");
        service.addStudent(name, branch, cgpa);
        HttpUtil.sendJson(ex, 201, "{\"message\":\"Student added.\"}");
    }

    private static void updateCgpa(HttpExchange ex) throws IOException, SQLException {
        Map<String, String> f = HttpUtil.body(ex);
        boolean done = service.updateCgpa(readInt(f, "id"), readCgpa(f, "cgpa"));
        reply(ex, done, "CGPA updated.", "No student with that id.");
    }

    private static void addDrive(HttpExchange ex) throws IOException, SQLException {
        Map<String, String> f = HttpUtil.body(ex);
        String company = readText(f, "company");
        String role = readText(f, "role");
        double minCgpa = readCgpa(f, "minCgpa");
        String date = readText(f, "date");
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Wrong date format. Use YYYY-MM-DD, e.g. 2026-11-20");
        }
        service.addDrive(company, role, minCgpa, date);
        HttpUtil.sendJson(ex, 201, "{\"message\":\"Drive added.\"}");
    }

    private static void eligible(HttpExchange ex) throws IOException, SQLException {
        int driveId = readInt(HttpUtil.query(ex), "driveId");
        Drive drive = service.getDriveById(driveId);
        if (drive == null) {
            HttpUtil.sendError(ex, 404, "No drive with id " + driveId);
            return;
        }
        List<Student> list = service.getEligibleStudents(driveId);
        HttpUtil.sendJson(ex, 200, "{\"drive\":" + Json.drive(drive) + ",\"students\":" + Json.students(list) + "}");
    }

    private static void reply(HttpExchange ex, boolean done, String ok, String notFound) throws IOException {
        if (done) {
            HttpUtil.sendJson(ex, 200, "{\"message\":" + Json.str(ok) + "}");
        } else {
            HttpUtil.sendError(ex, 404, notFound);
        }
    }

    // ---------- input checks (same rules as the console version) ----------

    private static String readText(Map<String, String> f, String key) {
        String v = f.getOrDefault(key, "");
        if (v.isEmpty()) {
            throw new IllegalArgumentException("Please fill in " + key + ".");
        }
        if (v.length() > 100) {
            throw new IllegalArgumentException(key + " is too long (max 100 characters).");
        }
        return v;
    }

    private static int readInt(Map<String, String> f, String key) {
        try {
            return Integer.parseInt(f.getOrDefault(key, ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a whole number.");
        }
    }

    private static double readCgpa(Map<String, String> f, String key) {
        try {
            double cgpa = Double.parseDouble(f.getOrDefault(key, ""));
            if (cgpa >= 0 && cgpa <= 10) {
                return cgpa;
            }
        } catch (NumberFormatException e) {
            // handled below
        }
        throw new IllegalArgumentException("CGPA must be a number between 0 and 10.");
    }
}
