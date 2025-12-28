package com.semisaad.smartstudy;

import com.semisaad.smartstudy.service.SpacedRepetitionService;
import com.semisaad.smartstudy.service.SpacedRepetitionService.ReviewResult;

public class SpacedRepetitionServiceTest {
    public static void main(String[] args) {
        SpacedRepetitionService service = new SpacedRepetitionService();

        System.out.println("=== SM-2 SPACED REPETITION ALGORITHM TEST ===\n");

        // Simulate a user learning a question over time
        System.out.println("📚 Scenario: User learning 'What is a Binary Search Tree?'\n");

        // Day 1: First time seeing the question
        System.out.println("Day 1: First review (never seen before)");
        System.out.println("User answers: CORRECT ✅");
        ReviewResult result = service.calculateFirstReview(true);
        System.out.println("Result: " + result);
        System.out.println("→ Next review in " + result.getIntervalDays() + " day(s)\n");

        System.out.println("---\n");

        // Day 2: Second review
        System.out.println("Day 2: Second review");
        System.out.println("User answers: CORRECT ✅");
        result = service.calculateNextReview(true, result.getEaseFactor(),
                result.getIntervalDays(), result.getRepetitions());
        System.out.println("Result: " + result);
        System.out.println("→ Next review in " + result.getIntervalDays() + " day(s)\n");

        System.out.println("---\n");

        // Day 8: Third review
        System.out.println("Day 8: Third review");
        System.out.println("User answers: CORRECT ✅");
        result = service.calculateNextReview(true, result.getEaseFactor(),
                result.getIntervalDays(), result.getRepetitions());
        System.out.println("Result: " + result);
        System.out.println("→ Next review in " + result.getIntervalDays() + " day(s)\n");

        System.out.println("---\n");

        // Day 24: Fourth review
        System.out.println("Day 24: Fourth review");
        System.out.println("User answers: WRONG ❌ (Forgot it!)");
        result = service.calculateNextReview(false, result.getEaseFactor(),
                result.getIntervalDays(), result.getRepetitions());
        System.out.println("Result: " + result);
        System.out.println("→ RESET! Next review in " + result.getIntervalDays() + " day(s)");
        System.out.println("→ Ease factor decreased to: " + result.getEaseFactor() + " (harder)\n");

        System.out.println("---\n");

        // Day 25: Review after forgetting
        System.out.println("Day 25: Review again after forgetting");
        System.out.println("User answers: CORRECT ✅");
        result = service.calculateNextReview(true, result.getEaseFactor(),
                result.getIntervalDays(), result.getRepetitions());
        System.out.println("Result: " + result);
        System.out.println("→ Next review in " + result.getIntervalDays() + " day(s)\n");

        System.out.println("---\n");

        // Test another scenario: Multiple wrong answers
        System.out.println("🔥 Scenario 2: Difficult question - multiple wrong answers\n");

        System.out.println("Attempt 1: WRONG ❌");
        ReviewResult difficult = service.calculateFirstReview(false);
        System.out.println("Ease Factor: " + difficult.getEaseFactor() + " | Interval: " + difficult.getIntervalDays());

        System.out.println("\nAttempt 2: WRONG ❌");
        difficult = service.calculateNextReview(false, difficult.getEaseFactor(),
                difficult.getIntervalDays(), difficult.getRepetitions());
        System.out.println("Ease Factor: " + difficult.getEaseFactor() + " | Interval: " + difficult.getIntervalDays());

        System.out.println("\nAttempt 3: WRONG ❌");
        difficult = service.calculateNextReview(false, difficult.getEaseFactor(),
                difficult.getIntervalDays(), difficult.getRepetitions());
        System.out.println("Ease Factor: " + difficult.getEaseFactor() + " | Interval: " + difficult.getIntervalDays());
        System.out.println("(Note: Ease factor can't go below 1.3)\n");

        System.out.println("\nAttempt 4: CORRECT ✅ (Finally!)");
        difficult = service.calculateNextReview(true, difficult.getEaseFactor(),
                difficult.getIntervalDays(), difficult.getRepetitions());
        System.out.println("Ease Factor: " + difficult.getEaseFactor() + " | Interval: " + difficult.getIntervalDays());

        System.out.println("\n=== TEST COMPLETE ===");
        System.out.println("\n💡 Key Takeaways:");
        System.out.println("- Correct answers → Longer intervals (1d → 6d → 15d → 39d...)");
        System.out.println("- Wrong answers → Reset to 1 day");
        System.out.println("- Ease factor adjusts based on performance");
        System.out.println("- This makes studying efficient - focus on what you struggle with!");
    }
}