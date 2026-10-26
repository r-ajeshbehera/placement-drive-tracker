package com.rajesh.placement.web;

import java.util.List;

import com.rajesh.placement.model.Drive;
import com.rajesh.placement.model.Student;

// Turns model objects into JSON text by hand (no library needed).
public final class Json {

    private Json() {
    }

    // a JSON string with quotes and special characters escaped
    public static String str(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

    public static String student(Student s) {
        return "{\"id\":" + s.getId()
                + ",\"name\":" + str(s.getName())
                + ",\"branch\":" + str(s.getBranch())
                + ",\"cgpa\":" + s.getCgpa() + "}";
    }

    public static String drive(Drive d) {
        return "{\"id\":" + d.getId()
                + ",\"company\":" + str(d.getCompany())
                + ",\"role\":" + str(d.getRole())
                + ",\"minCgpa\":" + d.getMinCgpa()
                + ",\"date\":" + str(d.getDriveDate()) + "}";
    }

    public static String students(List<Student> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(student(list.get(i)));
        }
        return sb.append(']').toString();
    }

    public static String drives(List<Drive> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(drive(list.get(i)));
        }
        return sb.append(']').toString();
    }
}
