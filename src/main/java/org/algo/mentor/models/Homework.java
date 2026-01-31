package org.algo.mentor.models;

import javafx.beans.property.*;

public class Homework {
    private int id;
    private int lessonId;
    private int studentId;
    private String studentName;
    private DoubleProperty score = new SimpleDoubleProperty(0.0);
    private StringProperty note = new SimpleStringProperty("");
    private Double originalScore;

    public Homework(int id, int lessonId, int studentId, String studentName, Double score, String note) {
        this.id = id;
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.originalScore = score;
        this.score.set(score != null ? score : 0.0);
        this.note.set(note != null ? note : "");
    }

    public int getId() { return id; }
    public int getLessonId() { return lessonId; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }

    public double getScore() { return score.get(); }
    public DoubleProperty scoreProperty() { return score; }
    public void setScore(double score) { 
        this.score.set(score);
        this.originalScore = score;
    }
    
    public void clearScore() {
        this.score.set(0.0);
        this.originalScore = null;
    }

    public String getNote() { return note.get(); }
    public StringProperty noteProperty() { return note; }
    public void setNote(String note) { this.note.set(note); }
    
    public Double getScoreForDatabase() {
        if (originalScore == null && score.get() == 0.0) {
            return null;
        }
        return score.get();
    }
    
    public boolean isGraded() {
        return originalScore != null;
    }
}
