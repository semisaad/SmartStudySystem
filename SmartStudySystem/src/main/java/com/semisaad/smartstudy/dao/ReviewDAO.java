package com.semisaad.smartstudy.dao;

import com.semisaad.smartstudy.database.DatabaseConnection;
import com.semisaad.smartstudy.model.Review;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    /**
     * Insert a new review into the database
     * @param review The review to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Review review) {
        String sql = "INSERT INTO reviews (question_id, user_id, reviewed_at, was_correct, ease_factor, interval_days, next_review_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, review.getQuestionId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setTimestamp(3, Timestamp.valueOf(review.getReviewedAt()));
            pstmt.setBoolean(4, review.isWasCorrect());
            pstmt.setFloat(5, review.getEaseFactor());
            pstmt.setInt(6, review.getIntervalDays());
            pstmt.setDate(7, Date.valueOf(review.getNextReviewDate()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all reviews from the database
     * @return List of all reviews
     */
    public List<Review> getAll() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews ORDER BY reviewed_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Review review = extractReviewFromResultSet(rs);
                reviews.add(review);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all reviews: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Get a review by its ID
     * @param id The review ID
     * @return Review object, or null if not found
     */
    public Review getById(int id) {
        String sql = "SELECT * FROM reviews WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Review review = extractReviewFromResultSet(rs);
                rs.close();
                return review;
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting review by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all reviews for a specific user
     * @param userId The user ID
     * @return List of reviews for that user
     */
    public List<Review> getByUserId(int userId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE user_id = ? ORDER BY reviewed_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Review review = extractReviewFromResultSet(rs);
                reviews.add(review);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting reviews by user: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Get all reviews for a specific question
     * @param questionId The question ID
     * @return List of reviews for that question
     */
    public List<Review> getByQuestionId(int questionId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE question_id = ? ORDER BY reviewed_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Review review = extractReviewFromResultSet(rs);
                reviews.add(review);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting reviews by question: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Get questions that are due for review today for a specific user
     * @param userId The user ID
     * @return List of question IDs that need review
     */
    public List<Integer> getDueQuestionIds(int userId) {
        List<Integer> questionIds = new ArrayList<>();

        // Fixed query: Remove ORDER BY or include it in SELECT
        String sql = "SELECT DISTINCT question_id FROM reviews " +
                "WHERE user_id = ? AND next_review_date <= CURRENT_DATE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                questionIds.add(rs.getInt("question_id"));
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting due questions: " + e.getMessage());
        }

        return questionIds;
    }

    /**
     * Get the most recent review for a specific question and user
     * @param questionId The question ID
     * @param userId The user ID
     * @return Most recent Review, or null if not found
     */
    public Review getLatestReview(int questionId, int userId) {
        String sql = "SELECT * FROM reviews WHERE question_id = ? AND user_id = ? " +
                "ORDER BY reviewed_at DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Review review = extractReviewFromResultSet(rs);
                rs.close();
                return review;
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting latest review: " + e.getMessage());
        }

        return null;
    }

    /**
     * Update an existing review
     * @param review The review with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(Review review) {
        String sql = "UPDATE reviews SET question_id = ?, user_id = ?, was_correct = ?, " +
                "ease_factor = ?, interval_days = ?, next_review_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, review.getQuestionId());
            pstmt.setInt(2, review.getUserId());
            pstmt.setBoolean(3, review.isWasCorrect());
            pstmt.setFloat(4, review.getEaseFactor());
            pstmt.setInt(5, review.getIntervalDays());
            pstmt.setDate(6, Date.valueOf(review.getNextReviewDate()));
            pstmt.setInt(7, review.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a review by its ID
     * @param id The review ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM reviews WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get total count of reviews
     * @return Number of reviews
     */
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM reviews";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error counting reviews: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Helper method to extract a Review object from a ResultSet
     * (Reduces code duplication)
     */
    private Review extractReviewFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int questionId = rs.getInt("question_id");
        int userId = rs.getInt("user_id");
        Timestamp timestamp = rs.getTimestamp("reviewed_at");
        LocalDateTime reviewedAt = timestamp.toLocalDateTime();
        boolean wasCorrect = rs.getBoolean("was_correct");
        float easeFactor = rs.getFloat("ease_factor");
        int intervalDays = rs.getInt("interval_days");
        Date date = rs.getDate("next_review_date");
        LocalDate nextReviewDate = date.toLocalDate();

        return new Review(id, questionId, userId, reviewedAt, wasCorrect,
                easeFactor, intervalDays, nextReviewDate);
    }
}