package com.rajesh.placement.model;

public class Student {
    private int id;
    private String name;
    private String branch;
    private double cgpa;

    public Student(int id, String name, String branch, double cgpa) {
        this.id = id;
        this.name = name;
        this.branch = branch;
        this.cgpa = cgpa;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }

    public double getCgpa() {
        return cgpa;
    }

    @Override
    public String toString() {
        return String.format("%-3d %-15s %-6s CGPA %.2f", id, name, branch, cgpa);
    }
}
