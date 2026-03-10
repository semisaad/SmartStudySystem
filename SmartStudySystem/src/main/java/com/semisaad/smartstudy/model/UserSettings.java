package com.semisaad.smartstudy.model;

import java.time.LocalDateTime;

public class UserSettings {
    private int userId;
    private int dailyGoal;
    private boolean remindersEnabled;
    private String theme;
    private String notificationTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UserSettings() {
        this.dailyGoal = 10;
        this.remindersEnabled = true;
        this.theme = "light";
        this.notificationTime = "09:00";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserSettings(int userId, int dailyGoal, boolean remindersEnabled, String theme, String notificationTime) {
        this.userId = userId;
        this.dailyGoal = dailyGoal;
        this.remindersEnabled = remindersEnabled;
        this.theme = theme;
        this.notificationTime = notificationTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(int dailyGoal) {
        this.dailyGoal = dailyGoal;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isRemindersEnabled() {
        return remindersEnabled;
    }

    public void setRemindersEnabled(boolean remindersEnabled) {
        this.remindersEnabled = remindersEnabled;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "userId=" + userId +
                ", dailyGoal=" + dailyGoal +
                ", remindersEnabled=" + remindersEnabled +
                ", theme='" + theme + '\'' +
                ", notificationTime='" + notificationTime + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}