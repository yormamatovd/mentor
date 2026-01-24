package org.algo.mentor.models;

import java.util.ArrayList;
import java.util.List;

public class QuestionSession {
    private int id;
    private int lessonId;
    private String topic;
    private double pointPerCorrect;
    private List<QuestionResult> results = new ArrayList<>();

    public QuestionSession() {}

    public QuestionSession(int id, int lessonId, String topic, double pointPerCorrect) {
        this.id = id;
        this.lessonId = lessonId;
        this.topic = topic;
        this.pointPerCorrect = pointPerCorrect;
    }

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
