package org.algo.mentor.models;

import javafx.beans.property.*;

public class LessonDetail {
    private int id;
    private int lessonId;
    private int studentId;
    private String studentName;
    private BooleanProperty attendance = new SimpleBooleanProperty();
    private ObjectProperty<Double> homeworkScore = new SimpleObjectProperty<>();
    private StringProperty homeworkNote = new SimpleStringProperty("");
    private ObjectProperty<Double> testScore = new SimpleObjectProperty<>();

    public LessonDetail(int id, int lessonId, int studentId, String studentName, 
                        boolean attendance, Double homeworkScore, String homeworkNote, Double testScore) {
        this.id = id;
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.attendance.set(attendance);
        this.homeworkScore.set(homeworkScore);
        this.homeworkNote.set(homeworkNote);
        this.testScore.set(testScore);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public boolean isAttendance() {
        return attendance.get();
    }

    public BooleanProperty attendanceProperty() {
        return attendance;
    }

    public void setAttendance(boolean attendance) {
        this.attendance.set(attendance);
    }

    public Double getHomeworkScore() {
        return homeworkScore.get();
    }

    public ObjectProperty<Double> homeworkScoreProperty() {
        return homeworkScore;
    }

    public void setHomeworkScore(Double homeworkScore) {
        this.homeworkScore.set(homeworkScore);
    }

    public String getHomeworkNote() {
        return homeworkNote.get();
    }

    public StringProperty homeworkNoteProperty() {
        return homeworkNote;
    }

    public void setHomeworkNote(String homeworkNote) {
        this.homeworkNote.set(homeworkNote);
    }

    public Double getTestScore() {
        return testScore.get();
    }

    public ObjectProperty<Double> testScoreProperty() {
        return testScore;
    }

    public void setTestScore(Double testScore) {
        this.testScore.set(testScore);
    }
}
