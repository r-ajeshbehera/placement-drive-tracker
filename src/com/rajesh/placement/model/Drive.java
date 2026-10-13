package com.rajesh.placement.model;

public class Drive {
    private int id;
    private String company;
    private String role;
    private double minCgpa;
    private String driveDate;

    public Drive(int id, String company, String role, double minCgpa, String driveDate) {
        this.id = id;
        this.company = company;
        this.role = role;
        this.minCgpa = minCgpa;
        this.driveDate = driveDate;
    }

    public int getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getRole() {
        return role;
    }

    public double getMinCgpa() {
        return minCgpa;
    }

    public String getDriveDate() {
        return driveDate;
    }

    @Override
    public String toString() {
        return String.format("%-3d %-12s %-28s Min CGPA %.1f  %s", id, company, role, minCgpa, driveDate);
    }
}
