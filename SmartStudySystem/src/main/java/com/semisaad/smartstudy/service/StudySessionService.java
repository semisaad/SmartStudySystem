package com.semisaad.smartstudy.service;

import com.semisaad.smartstudy.dao.QuestionDAO;
import com.semisaad.smartstudy.dao.ReviewDAO;
import com.semisaad.smartstudy.model.Question;
import com.semisaad.smartstudy.model.Review;
import com.semisaad.smartstudy.service.SpacedRepetitionService.ReviewResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StudySessionService {

    private final QuestionDAO questionDAO;
    private final ReviewDAO reviewDAO;
    private final SpacedRepetitionService spacedRepetitionService;

    public StudySessionService() {
        this.questionDAO = new QuestionDAO();
        this.reviewDAO = new ReviewDAO();
        this.spacedRepetitionService = new SpacedRepetitionService();
    }

    /**
     * Get questions due for review today, capped by the daily goal limit.
     */
    public List<Question> getDueQuestions(int userId) {
        List<Integer> dueQuestionIds = reviewDAO.getDueQuestionIds(userId);

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
     * Get questions that have NEVER been reviewed by this user,
     * excluding any question IDs already handled this session.
     */
    public List<Question> getNewQuestions(int userId, int limit, List<Integer> excludeIds) {
        List<Question> allQuestions = questionDAO.getAll();
        List<Question> newQuestions = new ArrayList<>();

        for (Question question : allQuestions) {
            // Skip if already done this session
            if (excludeIds.contains(question.getId())) {
                continue;
            }

            Review latestReview = reviewDAO.getLatestReview(question.getId(), userId);
            if (latestReview == null) {
                newQuestions.add(question);
                if (newQuestions.size() >= limit) {
                    break;
                }
            }
        }

        return newQuestions;
    }

    /**
     * Overload for backward compatibility — no exclusions.
     */
    public List<Question> getNewQuestions(int userId, int limit) {
        return getNewQuestions(userId, limit, new ArrayList<>());
    }

    /**
     * Submit answer, save review with correct SM-2 calculation.
     */
    public boolean submitAnswer(int questionId, int userId, boolean wasCorrect) {
        Review latestReview = reviewDAO.getLatestReview(questionId, userId);

        ReviewResult result;

        if (latestReview == null) {
            result = spacedRepetitionService.calculateFirstReview(wasCorrect);
        } else {
            result = spacedRepetitionService.calculateNextReview(
                    wasCorrect,
                    latestReview.getEaseFactor(),
                    latestReview.getIntervalDays(),
                    getRepetitionCount(questionId, userId)
            );
        }

        Review newReview = new Review(
                questionId,
                userId,
                wasCorrect,
                result.getEaseFactor(),
                result.getIntervalDays(),
                result.getNextReviewDate()
        );

        return reviewDAO.insert(newReview);
    }

    /**
     * FIXED: Sort reviews by date descending before counting
     * consecutive correct answers, so old wrong answers don't
     * incorrectly reset the streak count.
     */
    private int getRepetitionCount(int questionId, int userId) {
        List<Review> reviews = reviewDAO.getByQuestionId(questionId);

        // Sort most-recent first so we count the current streak correctly
        reviews.sort(Comparator.comparing(
                r -> r.getReviewedAt().toLocalDate(),
                Comparator.reverseOrder()
        ));

        int count = 0;
        for (Review review : reviews) {
            if (review.getUserId() == userId) {
                if (review.isWasCorrect()) {
                    count++;
                } else {
                    break; // Now correctly stops at most recent wrong answer
                }
            }
        }

        return count;
    }

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

        public int getTotalReviews() { return totalReviews; }
        public int getCorrectAnswers() { return correctAnswers; }
        public int getQuestionsDueToday() { return questionsDueToday; }
        public double getSuccessRate() { return successRate; }

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