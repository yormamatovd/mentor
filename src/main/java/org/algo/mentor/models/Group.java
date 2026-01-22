package org.algo.mentor.models;

public class Group {
    private int id;
    private String name;
    private int studentCount;

    public Group(int id, String name, int studentCount) {
        this.id = id;
        this.name = name;
        this.studentCount = studentCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return name;
    }
}
