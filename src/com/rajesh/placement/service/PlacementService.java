package com.rajesh.placement.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rajesh.placement.db.DBConnection;
import com.rajesh.placement.model.Drive;
import com.rajesh.placement.model.Student;

public class PlacementService {

    // ---------- students ----------

    public void addStudent(String name, String branch, double cgpa) throws SQLException {
        String sql = "INSERT INTO student (name, branch, cgpa) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, branch);
            ps.setDouble(3, cgpa);
            ps.executeUpdate();
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        return findStudents("SELECT * FROM student ORDER BY cgpa DESC", null);
    }

    public boolean updateCgpa(int id, double newCgpa) throws SQLException {
        String sql = "UPDATE student SET cgpa = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newCgpa);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM student WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- drives ----------

    public void addDrive(String company, String role, double minCgpa, String date) throws SQLException {
        String sql = "INSERT INTO drive (company, role, min_cgpa, drive_date) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, company);
            ps.setString(2, role);
            ps.setDouble(3, minCgpa);
            ps.setString(4, date);
            ps.executeUpdate();
        }
    }

    public List<Drive> getAllDrives() throws SQLException {
        return findDrives("SELECT * FROM drive ORDER BY drive_date", null);
    }

    public List<Drive> searchDrives(String company) throws SQLException {
        return findDrives("SELECT * FROM drive WHERE company LIKE ?", "%" + company + "%");
    }

    public Drive getDriveById(int id) throws SQLException {
        List<Drive> list = findDrives("SELECT * FROM drive WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    // students whose CGPA is at least the drive's minimum, best first
    public List<Student> getEligibleStudents(int driveId) throws SQLException {
        String sql = "SELECT s.* FROM student s JOIN drive d ON s.cgpa >= d.min_cgpa "
                   + "WHERE d.id = ? ORDER BY s.cgpa DESC";
        return findStudents(sql, driveId);
    }

    // ---------- helpers ----------

    private List<Student> findStudents(String sql, Object param) throws SQLException {
        List<Student> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setObject(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(rs.getInt("id"), rs.getString("name"),
                            rs.getString("branch"), rs.getDouble("cgpa")));
                }
            }
        }
        return list;
    }

    private List<Drive> findDrives(String sql, Object param) throws SQLException {
        List<Drive> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setObject(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Drive(rs.getInt("id"), rs.getString("company"), rs.getString("role"),
                            rs.getDouble("min_cgpa"), rs.getString("drive_date")));
                }
            }
        }
        return list;
    }
}
