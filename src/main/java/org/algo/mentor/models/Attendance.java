package org.algo.mentor.models;

import javafx.beans.property.*;

public class Attendance {
    private int id;
    private int lessonId;
    private int studentId;
    private String studentName;
    private BooleanProperty present = new SimpleBooleanProperty(false);
    private DoubleProperty totalScore = new SimpleDoubleProperty(0.0);

    public Attendance(int id, int lessonId, int studentId, String studentName, boolean present) {
        this.id = id;
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.present.set(present);
    }

    public int getId() { return id; }
    public int getLessonId() { return lessonId; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }

    public boolean isPresent() { return present.get(); }
    public BooleanProperty presentProperty() { return present; }
    public void setPresent(boolean present) { this.present.set(present); }

    public double getTotalScore() { return totalScore.get(); }
    public DoubleProperty totalScoreProperty() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore.set(totalScore); }
}
