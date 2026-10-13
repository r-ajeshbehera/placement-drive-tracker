package com.rajesh.placement;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import com.rajesh.placement.model.Drive;
import com.rajesh.placement.model.Student;
import com.rajesh.placement.service.PlacementService;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final PlacementService service = new PlacementService();

    public static void main(String[] args) {
        while (true) {
            printMenu();
            try {
                int choice = readInt("Enter choice: ");
                switch (choice) {
                    case 1 -> addStudent();
                    case 2 -> printList(service.getAllStudents());
                    case 3 -> updateCgpa();
                    case 4 -> deleteStudent();
                    case 5 -> addDrive();
                    case 6 -> printList(service.getAllDrives());
                    case 7 -> printList(service.searchDrives(readText("Company name: ")));
                    case 8 -> showEligible();
                    case 0 -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== PLACEMENT DRIVE TRACKER =====");
        System.out.println("1. Add student");
        System.out.println("2. View all students");
        System.out.println("3. Update student CGPA");
        System.out.println("4. Delete student");
        System.out.println("5. Add placement drive");
        System.out.println("6. View all drives");
        System.out.println("7. Search drive by company");
        System.out.println("8. Show eligible students for a drive");
        System.out.println("0. Exit");
    }

    private static void addStudent() throws SQLException {
        String name = readText("Name: ");
        String branch = readText("Branch: ");
        double cgpa = readCgpa("CGPA: ");
        service.addStudent(name, branch, cgpa);
        System.out.println("Student added.");
    }

    private static void updateCgpa() throws SQLException {
        int id = readInt("Student id: ");
        double cgpa = readCgpa("New CGPA: ");
        System.out.println(service.updateCgpa(id, cgpa) ? "CGPA updated." : "No student with that id.");
    }

    private static void deleteStudent() throws SQLException {
        int id = readInt("Student id to delete: ");
        System.out.println(service.deleteStudent(id) ? "Deleted." : "No student with that id.");
    }

    private static void addDrive() throws SQLException {
        String company = readText("Company: ");
        String role = readText("Role: ");
        double minCgpa = readCgpa("Min CGPA: ");
        String date = readText("Date (YYYY-MM-DD): ");
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            System.out.println("Wrong date format. Use YYYY-MM-DD, e.g. 2026-10-15");
            return;
        }
        service.addDrive(company, role, minCgpa, date);
        System.out.println("Drive added.");
    }

    private static void showEligible() throws SQLException {
        int driveId = readInt("Drive id: ");
        Drive drive = service.getDriveById(driveId);
        if (drive == null) {
            System.out.println("No drive with id " + driveId);
            return;
        }
        List<Student> eligible = service.getEligibleStudents(driveId);
        System.out.println(eligible.size() + " student(s) eligible for " + drive.getCompany()
                + " (min CGPA " + drive.getMinCgpa() + "):");
        printList(eligible);
    }

    // ---------- input helpers ----------

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = sc.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        return Integer.parseInt(sc.nextLine().trim());
    }

    private static double readCgpa(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double cgpa = Double.parseDouble(sc.nextLine().trim());
                if (cgpa >= 0 && cgpa <= 10) {
                    return cgpa;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("CGPA must be a number between 0 and 10.");
        }
    }

    private static void printList(List<?> list) {
        if (list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Object item : list) {
            System.out.println(item);
        }
    }
}
