package org.algo.mentor.models;

import java.util.ArrayList;
import java.util.List;

public class QuestionSession {
    private int id;
    private int lessonId;
    private String topic;
    private double pointPerCorrect = 1.0;
    private int totalQuestions;
    private List<QuestionResult> results = new ArrayList<>();

    public QuestionSession() {}

    public QuestionSession(int id, int lessonId, String topic, int totalQuestions) {
        this.id = id;
        this.lessonId = lessonId;
        this.topic = topic;
        this.totalQuestions = totalQuestions;
    }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public double getPointPerCorrect() { return pointPerCorrect; }
    public void setPointPerCorrect(double pointPerCorrect) { this.pointPerCorrect = pointPerCorrect; }

    public List<QuestionResult> getResults() { return results; }
    public void setResults(List<QuestionResult> results) { this.results = results; }
}
