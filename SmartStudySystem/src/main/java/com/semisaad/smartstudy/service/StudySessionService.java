package com.semisaad.smartstudy.service;

import com.semisaad.smartstudy.dao.QuestionDAO;
import com.semisaad.smartstudy.dao.ReviewDAO;
import com.semisaad.smartstudy.model.Question;
import com.semisaad.smartstudy.model.Review;
import com.semisaad.smartstudy.service.SpacedRepetitionService.ReviewResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages study sessions - getting due questions, submitting answers, tracking progress
 */
public class StudySessionService {

    private final QuestionDAO questionDAO;
    private final ReviewDAO reviewDAO;
    private final SpacedRepetitionService spacedRepetitionService;

    // Constructor
    public StudySessionService() {
        this.questionDAO = new QuestionDAO();
        this.reviewDAO = new ReviewDAO();
        this.spacedRepetitionService = new SpacedRepetitionService();
    }

    /**
     * Get all questions that are due for review today for a user
     *
     * @param userId The user ID
     * @return List of questions due for review
     */
    public List<Question> getDueQuestions(int userId) {
        // Get question IDs that are due
        List<Integer> dueQuestionIds = reviewDAO.getDueQuestionIds(userId);

        // Fetch the actual Question objects
        List<Question> dueQuestions = new ArrayList<>();
        for (int questionId : dueQuestionIds) {
            Question question = questionDAO.getById(questionId);
            if (question != null) {
                dueQuestions.add(question);
            }
        }

        return dueQuestions;
    }

    /**
     * Get questions that have NEVER been reviewed by this user
     * (New questions to learn)
     *
     * @param userId The user ID
     * @param limit Maximum number of new questions to return
     * @return List of new questions
     */
    public List<Question> getNewQuestions(int userId, int limit) {
        List<Question> allQuestions = questionDAO.getAll();
        List<Question> newQuestions = new ArrayList<>();

        // Find questions this user has never reviewed
        for (Question question : allQuestions) {
            Review latestReview = reviewDAO.getLatestReview(question.getId(), userId);

            if (latestReview == null) {
                // Never reviewed = new question!
                newQuestions.add(question);

                if (newQuestions.size() >= limit) {
                    break;
                }
            }
        }

        return newQuestions;
    }

    /**
     * Submit a user's answer to a question
     * This calculates the next review date using SM-2 and saves it
     *
     * @param questionId The question ID
     * @param userId The user ID
     * @param wasCorrect Did the user answer correctly?
     * @return true if successful, false otherwise
     */
    public boolean submitAnswer(int questionId, int userId, boolean wasCorrect) {
        // Get the latest review for this question (if it exists)
        Review latestReview = reviewDAO.getLatestReview(questionId, userId);

        ReviewResult result;

        if (latestReview == null) {
            // First time reviewing this question
            result = spacedRepetitionService.calculateFirstReview(wasCorrect);
        } else {
            // Calculate based on previous review
            result = spacedRepetitionService.calculateNextReview(
                    wasCorrect,
                    latestReview.getEaseFactor(),
                    latestReview.getIntervalDays(),
                    getRepetitionCount(questionId, userId)
            );
        }

        // Create a new review record
        Review newReview = new Review(
                questionId,
                userId,
                wasCorrect,
                result.getEaseFactor(),
                result.getIntervalDays(),
                result.getNextReviewDate()
        );

        // Save it to database
        return reviewDAO.insert(newReview);
    }

    /**
     * Get the number of consecutive correct answers for a question
     *
     * @param questionId The question ID
     * @param userId The user ID
     * @return Number of consecutive correct reviews
     */
    private int getRepetitionCount(int questionId, int userId) {
        List<Review> reviews = reviewDAO.getByQuestionId(questionId);

        // Count consecutive correct answers from most recent
        int count = 0;
        for (Review review : reviews) {
            if (review.getUserId() == userId) {
                if (review.isWasCorrect()) {
                    count++;
                } else {
                    break; // Stop at first wrong answer
                }
            }
        }

        return count;
    }

    /**
     * Get study session statistics for a user
     *
     * @param userId The user ID
     * @return SessionStats object with various metrics
     */
    public SessionStats getSessionStats(int userId) {
        List<Review> userReviews = reviewDAO.getByUserId(userId);

        int totalReviews = userReviews.size();
        int correctCount = 0;
        int dueToday = getDueQuestions(userId).size();

        for (Review review : userReviews) {
            if (review.isWasCorrect()) {
                correctCount++;
            }
        }

        double successRate = totalReviews > 0 ? (correctCount * 100.0 / totalReviews) : 0.0;

        return new SessionStats(totalReviews, correctCount, dueToday, successRate);
    }

    /**
     * Get questions for a specific topic that are due for review
     *
     * @param userId The user ID
     * @param topicId The topic ID
     * @return List of due questions for that topic
     */
    public List<Question> getDueQuestionsByTopic(int userId, int topicId) {
        List<Question> allDueQuestions = getDueQuestions(userId);
        List<Question> topicDueQuestions = new ArrayList<>();

        for (Question question : allDueQuestions) {
            if (question.getTopicId() == topicId) {
                topicDueQuestions.add(question);
            }
        }

        return topicDueQuestions;
    }

    /**
     * Inner class to hold session statistics
     */
    public static class SessionStats {
        private final int totalReviews;
        private final int correctAnswers;
        private final int questionsDueToday;
        private final double successRate;

        public SessionStats(int totalReviews, int correctAnswers, int questionsDueToday, double successRate) {
            this.totalReviews = totalReviews;
            this.correctAnswers = correctAnswers;
            this.questionsDueToday = questionsDueToday;
            this.successRate = successRate;
        }

        public int getTotalReviews() {
            return totalReviews;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getQuestionsDueToday() {
            return questionsDueToday;
        }

        public double getSuccessRate() {
            return successRate;
        }

        @Override
        public String toString() {
            return "SessionStats{" +
                    "totalReviews=" + totalReviews +
                    ", correctAnswers=" + correctAnswers +
                    ", questionsDueToday=" + questionsDueToday +
                    ", successRate=" + String.format("%.1f", successRate) + "%" +
                    '}';
        }
    }
}