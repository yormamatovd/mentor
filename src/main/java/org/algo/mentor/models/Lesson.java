package org.algo.mentor.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lesson {
    private int id;
    private int groupId;
    private LocalDateTime lessonDate;
    private String topic;

    public Lesson(int id, int groupId, LocalDateTime lessonDate) {
        this(id, groupId, lessonDate, lessonDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    public Lesson(int id, int groupId, LocalDateTime lessonDate, String topic) {
        this.id = id;
        this.groupId = groupId;
        this.lessonDate = lessonDate;
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public LocalDateTime getLessonDate() {
        return lessonDate;
    }

    public void setLessonDate(LocalDateTime lessonDate) {
        this.lessonDate = lessonDate;
    }
}
