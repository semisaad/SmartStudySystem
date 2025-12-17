package com.semisaad.smartstudy.model;

import java.time.LocalDateTime;

public class Question {
    private int id;
    private String questionText;
    private String answer;
    private int topicId;
    private String difficulty;
    private LocalDateTime createdAt;

    // Constructor for existing questions from database
    public Question(int id, String questionText, String answer, int topicId,
                    String difficulty, LocalDateTime createdAt) {
        this.id = id;
        this.questionText = questionText;
        this.answer = answer;
        this.topicId = topicId;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
    }

    // Constructor for new questions
    public Question(String questionText, String answer, int topicId, String difficulty) {
        this.questionText = questionText;
        this.answer = answer;
        this.topicId = topicId;
        this.difficulty = difficulty;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getAnswer() {
        return answer;
    }

    public int getTopicId() {
        return topicId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // toString for debugging
    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", answer='" + answer + '\'' +
                ", topicId=" + topicId +
                ", difficulty='" + difficulty + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}