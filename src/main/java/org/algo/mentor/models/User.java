package org.algo.mentor.models;

public class User {
    private String username;
    private String fullName;
    private String avatar;

    public User(String username, String fullName, String avatar) {
        this.username = username;
        this.fullName = fullName;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
