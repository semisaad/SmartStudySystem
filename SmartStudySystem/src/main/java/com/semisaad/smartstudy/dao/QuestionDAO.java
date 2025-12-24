package com.semisaad.smartstudy.dao;

import com.semisaad.smartstudy.database.DatabaseConnection;
import com.semisaad.smartstudy.model.Question;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    /**
     * Insert a new question into the database
     * @param question The question to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Question question) {
        String sql = "INSERT INTO questions (question_text, answer, topic_id, difficulty, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, question.getQuestionText());
            pstmt.setString(2, question.getAnswer());
            pstmt.setInt(3, question.getTopicId());
            pstmt.setString(4, question.getDifficulty());
            pstmt.setTimestamp(5, Timestamp.valueOf(question.getCreatedAt()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all questions from the database
     * @return List of all questions
     */
    public List<Question> getAll() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String questionText = rs.getString("question_text");
                String answer = rs.getString("answer");
                int topicId = rs.getInt("topic_id");
                String difficulty = rs.getString("difficulty");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                Question question = new Question(id, questionText, answer, topicId, difficulty, createdAt);
                questions.add(question);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all questions: " + e.getMessage());
        }

        return questions;
    }

    /**
     * Get a question by its ID
     * @param id The question ID
     * @return Question object, or null if not found
     */
    public Question getById(int id) {
        String sql = "SELECT * FROM questions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String questionText = rs.getString("question_text");
                String answer = rs.getString("answer");
                int topicId = rs.getInt("topic_id");
                String difficulty = rs.getString("difficulty");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                rs.close();
                return new Question(id, questionText, answer, topicId, difficulty, createdAt);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting question by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all questions for a specific topic
     * @param topicId The topic ID
     * @return List of questions for that topic
     */
    public List<Question> getByTopicId(int topicId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE topic_id = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, topicId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String questionText = rs.getString("question_text");
                String answer = rs.getString("answer");
                String difficulty = rs.getString("difficulty");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                Question question = new Question(id, questionText, answer, topicId, difficulty, createdAt);
                questions.add(question);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting questions by topic: " + e.getMessage());
        }

        return questions;
    }

    /**
     * Get all questions with a specific difficulty
     * @param difficulty The difficulty level (EASY, MEDIUM, HARD)
     * @return List of questions with that difficulty
     */
    public List<Question> getByDifficulty(String difficulty) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE difficulty = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, difficulty);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String questionText = rs.getString("question_text");
                String answer = rs.getString("answer");
                int topicId = rs.getInt("topic_id");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                Question question = new Question(id, questionText, answer, topicId, difficulty, createdAt);
                questions.add(question);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting questions by difficulty: " + e.getMessage());
        }

        return questions;
    }

    /**
     * Update an existing question
     * @param question The question with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(Question question) {
        String sql = "UPDATE questions SET question_text = ?, answer = ?, topic_id = ?, difficulty = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, question.getQuestionText());
            pstmt.setString(2, question.getAnswer());
            pstmt.setInt(3, question.getTopicId());
            pstmt.setString(4, question.getDifficulty());
            pstmt.setInt(5, question.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a question by its ID
     * @param id The question ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get total count of questions in database
     * @return Number of questions
     */
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM questions";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error counting questions: " + e.getMessage());
        }

        return 0;
    }
}