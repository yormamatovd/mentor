package org.algo.mentor.models;

import java.time.LocalDateTime;

public class Lesson {
    private int id;
    private int groupId;
    private LocalDateTime lessonDate;

    public Lesson(int id, int groupId, LocalDateTime lessonDate) {
        this.id = id;
        this.groupId = groupId;
        this.lessonDate = lessonDate;
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
