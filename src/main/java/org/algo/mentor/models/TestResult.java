package org.algo.mentor.models;

import javafx.beans.property.*;

/**
 * Bitta o'quvchining bitta test sessiyasidagi natijasi
 */
public class TestResult {
    private int id;
    private int testSessionId;
    private int studentId;
    private String studentName; // UI uchun qulaylik
    private StringProperty section = new SimpleStringProperty("");
    private IntegerProperty correctCount = new SimpleIntegerProperty(0);
    private DoubleProperty totalScore = new SimpleDoubleProperty(0.0);

    public TestResult(int id, int testSessionId, int studentId, String studentName, String section, int correctCount, double totalScore) {
        this.id = id;
        this.testSessionId = testSessionId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.section.set(section != null ? section : "");
        this.correctCount.set(correctCount);
        this.totalScore.set(totalScore);
    }

    public int getId() { return id; }
    public int getTestSessionId() { return testSessionId; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }

    public String getSection() { return section.get(); }
    public StringProperty sectionProperty() { return section; }
    public void setSection(String section) { this.section.set(section); }

    public int getCorrectCount() { return correctCount.get(); }
    public IntegerProperty correctCountProperty() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount.set(correctCount); }

    public double getTotalScore() { return totalScore.get(); }
    public DoubleProperty totalScoreProperty() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore.set(totalScore); }
}
