package com.semisaad.smartstudy;

import com.semisaad.smartstudy.dao.ReviewDAO;
import com.semisaad.smartstudy.dao.QuestionDAO;
import com.semisaad.smartstudy.dao.UserDAO;
import com.semisaad.smartstudy.model.Review;
import com.semisaad.smartstudy.model.Question;
import com.semisaad.smartstudy.model.User;

import java.time.LocalDate;
import java.util.List;

public class ReviewDAOTest {
    public static void main(String[] args) {
        ReviewDAO reviewDAO = new ReviewDAO();
        QuestionDAO questionDAO = new QuestionDAO();
        UserDAO userDAO = new UserDAO();

        System.out.println("=== COMPLETE ReviewDAO TEST ===\n");

        // Test 1: Get count
        System.out.println("📊 Test 1: Get Review Count");
        int count = reviewDAO.getCount();
        System.out.println("Total reviews in database: " + count);

        System.out.println("\n---\n");

        // Test 2: Get all reviews
        System.out.println("📋 Test 2: Get All Reviews");
        List<Review> allReviews = reviewDAO.getAll();
        System.out.println("Found " + allReviews.size() + " reviews");

        System.out.println("\n---\n");

        // Test 3: Insert new review
        System.out.println("➕ Test 3: Insert New Review");

// Get first question and first user
        Question question = questionDAO.getById(1);
        User user = userDAO.getById(1);

        if (question != null && user != null) {
            System.out.println("Creating review for:");

            // Safe string preview
            String questionPreview = question.getQuestionText();
            if (questionPreview.length() > 50) {
                questionPreview = questionPreview.substring(0, 50) + "...";
            }
            System.out.println("  Question: " + questionPreview);
            System.out.println("  User: " + user.getUsername());

            // Create a review: user answered CORRECTLY
            Review newReview = new Review(
                    question.getId(),
                    user.getId(),
                    true,                    // was_correct = true
                    2.5f,                    // ease_factor = 2.5 (default)
                    1,                       // interval_days = 1 (show again tomorrow)
                    LocalDate.now().plusDays(1)  // next_review_date = tomorrow
            );

            boolean inserted = reviewDAO.insert(newReview);
            System.out.println(inserted ? "✅ Review inserted successfully!" : "❌ Insert failed!");
        } else {
            System.out.println("⚠️ Could not find question or user for testing!");
        }
        System.out.println("\n---\n");

        // Test 4: Get reviews by user
        System.out.println("🔍 Test 4: Get Reviews by User (User ID = 1)");
        List<Review> userReviews = reviewDAO.getByUserId(1);
        System.out.println("Found " + userReviews.size() + " reviews for user");
        for (Review r : userReviews) {
            System.out.println("  - Question " + r.getQuestionId() +
                    " | Correct: " + r.isWasCorrect() +
                    " | Next review: " + r.getNextReviewDate());
        }

        System.out.println("\n---\n");

        // Test 5: Get reviews by question
        System.out.println("📝 Test 5: Get Reviews by Question (Question ID = 1)");
        List<Review> questionReviews = reviewDAO.getByQuestionId(1);
        System.out.println("Found " + questionReviews.size() + " reviews for this question");

        System.out.println("\n---\n");

        // Test 6: Get latest review for a question/user
        System.out.println("🕐 Test 6: Get Latest Review (Question 1, User 1)");
        Review latestReview = reviewDAO.getLatestReview(1, 1);
        if (latestReview != null) {
            System.out.println("Latest review:");
            System.out.println("  Reviewed at: " + latestReview.getReviewedAt());
            System.out.println("  Was correct: " + latestReview.isWasCorrect());
            System.out.println("  Ease factor: " + latestReview.getEaseFactor());
            System.out.println("  Interval days: " + latestReview.getIntervalDays());
            System.out.println("  Next review: " + latestReview.getNextReviewDate());
        } else {
            System.out.println("No review found!");
        }

        System.out.println("\n---\n");

        // Test 7: Get due questions (questions that need review today)
        System.out.println("⏰ Test 7: Get Due Questions for User 1");
        List<Integer> dueQuestions = reviewDAO.getDueQuestionIds(1);
        System.out.println("Questions due for review: " + dueQuestions.size());
        if (!dueQuestions.isEmpty()) {
            System.out.println("Due question IDs: " + dueQuestions);
        } else {
            System.out.println("No questions due for review today!");
        }

        System.out.println("\n---\n");

        // Test 8: Update review
        System.out.println("✏️ Test 8: Update Review");
        if (latestReview != null) {
            // Simulate: user reviewed again and got it correct
            latestReview.setWasCorrect(true);
            latestReview.setEaseFactor(2.6f);  // Increase ease factor
            latestReview.setIntervalDays(6);   // Increase interval
            latestReview.setNextReviewDate(LocalDate.now().plusDays(6));

            boolean updated = reviewDAO.update(latestReview);
            System.out.println(updated ? "✅ Update successful!" : "❌ Update failed!");

            Review updatedReview = reviewDAO.getById(latestReview.getId());
            System.out.println("New ease factor: " + updatedReview.getEaseFactor());
            System.out.println("New interval: " + updatedReview.getIntervalDays() + " days");
        }

        System.out.println("\n---\n");

        // Test 9: Delete review
        System.out.println("🗑️ Test 9: Delete Review");

        // Find the review we just created
        allReviews = reviewDAO.getAll();
        Review testReview = null;
        for (Review r : allReviews) {
            if (r.getQuestionId() == 1 && r.getUserId() == 1) {
                testReview = r;
                break;
            }
        }

        if (testReview != null) {
            System.out.println("Found test review with ID: " + testReview.getId());
            boolean deleted = reviewDAO.delete(testReview.getId());
            System.out.println(deleted ? "✅ Delete successful!" : "❌ Delete failed!");

            int newCount = reviewDAO.getCount();
            System.out.println("Reviews remaining: " + newCount);
        } else {
            System.out.println("Test review not found (may have been deleted already)");
        }

        System.out.println("\n=== TEST COMPLETE ===");
    }
}


