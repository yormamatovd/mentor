package org.algo.mentor.models;

public class Student {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String telegramUsername;
    private String parentName;
    private String parentPhone;
    private String parentTelegram;
    private boolean isActive;

    public Student(int id, String firstName, String lastName, String phone, 
                   String telegramUsername, String parentName, String parentPhone, 
                   String parentTelegram, boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.telegramUsername = telegramUsername;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.parentTelegram = parentTelegram;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public void setTelegramUsername(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getParentTelegram() {
        return parentTelegram;
    }

    public void setParentTelegram(String parentTelegram) {
        this.parentTelegram = parentTelegram;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
