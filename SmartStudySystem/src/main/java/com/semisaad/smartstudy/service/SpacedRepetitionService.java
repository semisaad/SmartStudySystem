package com.semisaad.smartstudy.service;

import java.time.LocalDate;

/**
 * Implements the SM-2 (SuperMemo 2) spaced repetition algorithm
 * This calculates optimal review intervals based on user performance
 */
public class SpacedRepetitionService {

    // Constants for the algorithm
    private static final float MIN_EASE_FACTOR = 1.3f;
    private static final float DEFAULT_EASE_FACTOR = 2.5f;
    private static final int FIRST_INTERVAL = 1;      // 1 day
    private static final int SECOND_INTERVAL = 6;     // 6 days

    /**
     * Calculate the next review based on user's answer
     *
     * @param wasCorrect Did the user answer correctly?
     * @param currentEaseFactor Current ease factor for this question
     * @param currentInterval Current interval in days
     * @param repetitions Number of times answered correctly in a row
     * @return ReviewResult containing new values
     */
    public ReviewResult calculateNextReview(boolean wasCorrect, float currentEaseFactor,
                                            int currentInterval, int repetitions) {

        float newEaseFactor;
        int newInterval;
        int newRepetitions;

        if (wasCorrect) {
            // User answered CORRECTLY
            newRepetitions = repetitions + 1;

            // Adjust ease factor (make it slightly easier)
            // Formula: EF' = EF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
            // For correct answer, we use quality = 4 (good recall)
            newEaseFactor = currentEaseFactor + 0.1f;

            // Don't let ease factor get too high
            if (newEaseFactor > 3.0f) {
                newEaseFactor = 3.0f;
            }

            // Calculate new interval
            if (newRepetitions == 1) {
                newInterval = FIRST_INTERVAL;  // First time correct: 1 day
            } else if (newRepetitions == 2) {
                newInterval = SECOND_INTERVAL; // Second time correct: 6 days
            } else {
                // After that: multiply previous interval by ease factor
                newInterval = Math.round(currentInterval * newEaseFactor);
            }

        } else {
            // User answered INCORRECTLY
            newRepetitions = 0;  // Reset repetition count
            newInterval = FIRST_INTERVAL;  // Start over: review tomorrow

            // Adjust ease factor (make it harder)
            // Formula: decrease by 0.2
            newEaseFactor = currentEaseFactor - 0.2f;

            // Don't let ease factor go below minimum
            if (newEaseFactor < MIN_EASE_FACTOR) {
                newEaseFactor = MIN_EASE_FACTOR;
            }
        }

        // Calculate the actual next review date
        LocalDate nextReviewDate = LocalDate.now().plusDays(newInterval);

        return new ReviewResult(newEaseFactor, newInterval, newRepetitions, nextReviewDate);
    }

    /**
     * Calculate review for a brand new question (first time seeing it)
     *
     * @param wasCorrect Did the user answer correctly on first try?
     * @return ReviewResult with initial values
     */
    public ReviewResult calculateFirstReview(boolean wasCorrect) {
        return calculateNextReview(wasCorrect, DEFAULT_EASE_FACTOR, 0, 0);
    }

    /**
     * Result class to hold all the calculated values
     */
    public static class ReviewResult {
        private final float easeFactor;
        private final int intervalDays;
        private final int repetitions;
        private final LocalDate nextReviewDate;

        public ReviewResult(float easeFactor, int intervalDays, int repetitions, LocalDate nextReviewDate) {
            this.easeFactor = easeFactor;
            this.intervalDays = intervalDays;
            this.repetitions = repetitions;
            this.nextReviewDate = nextReviewDate;
        }

        public float getEaseFactor() {
            return easeFactor;
        }

        public int getIntervalDays() {
            return intervalDays;
        }

        public int getRepetitions() {
            return repetitions;
        }

        public LocalDate getNextReviewDate() {
            return nextReviewDate;
        }

        @Override
        public String toString() {
            return "ReviewResult{" +
                    "easeFactor=" + easeFactor +
                    ", intervalDays=" + intervalDays +
                    ", repetitions=" + repetitions +
                    ", nextReviewDate=" + nextReviewDate +
                    '}';
        }
    }
}