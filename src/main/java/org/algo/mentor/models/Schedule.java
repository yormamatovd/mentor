package org.algo.mentor.models;

public class Schedule {
    private int id;
    private int groupId;
    private String groupName;
    private int dayOfWeek; // 1-7 (Monday-Sunday)
    private String lessonTime;

    public Schedule(int id, int groupId, String groupName, int dayOfWeek, String lessonTime) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.dayOfWeek = dayOfWeek;
        this.lessonTime = lessonTime;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getLessonTime() {
        return lessonTime;
    }

    public void setLessonTime(String lessonTime) {
        this.lessonTime = lessonTime;
    }
}
