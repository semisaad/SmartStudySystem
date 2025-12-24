package com.semisaad.smartstudy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Review {
    private int id;
    private int questionId;
    private int userId;
    private LocalDateTime reviewedAt;
    private boolean wasCorrect;
    private float easeFactor;
    private int intervalDays;
    private LocalDate nextReviewDate;

    // Constructor for existing reviews from database
    public Review(int id, int questionId, int userId, LocalDateTime reviewedAt,
                  boolean wasCorrect, float easeFactor, int intervalDays, LocalDate nextReviewDate) {
        this.id = id;
        this.questionId = questionId;
        this.userId = userId;
        this.reviewedAt = reviewedAt;
        this.wasCorrect = wasCorrect;
        this.easeFactor = easeFactor;
        this.intervalDays = intervalDays;
        this.nextReviewDate = nextReviewDate;
    }

    // Constructor for new reviews
    public Review(int questionId, int userId, boolean wasCorrect,
                  float easeFactor, int intervalDays, LocalDate nextReviewDate) {
        this.questionId = questionId;
        this.userId = userId;
        this.reviewedAt = LocalDateTime.now();
        this.wasCorrect = wasCorrect;
        this.easeFactor = easeFactor;
        this.intervalDays = intervalDays;
        this.nextReviewDate = nextReviewDate;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getUserId() {
        return userId;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public boolean isWasCorrect() {
        return wasCorrect;
    }

    public float getEaseFactor() {
        return easeFactor;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public void setWasCorrect(boolean wasCorrect) {
        this.wasCorrect = wasCorrect;
    }

    public void setEaseFactor(float easeFactor) {
        this.easeFactor = easeFactor;
    }

    public void setIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    // toString for debugging
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", userId=" + userId +
                ", reviewedAt=" + reviewedAt +
                ", wasCorrect=" + wasCorrect +
                ", easeFactor=" + easeFactor +
                ", intervalDays=" + intervalDays +
                ", nextReviewDate=" + nextReviewDate +
                '}';
    }
}