package com.semisaad.smartstudy;

import com.semisaad.smartstudy.model.Question;
import com.semisaad.smartstudy.service.StudySessionService;
import com.semisaad.smartstudy.service.StudySessionService.SessionStats;

import java.util.List;
import java.util.Scanner;

public class StudySessionServiceTest {
    public static void main(String[] args) {
        StudySessionService sessionService = new StudySessionService();
        Scanner scanner = new Scanner(System.in);

        int userId = 1; // Using user ID 1 (saad)

        System.out.println("=== STUDY SESSION SERVICE TEST ===\n");

        // Test 1: Get current stats
        System.out.println("📊 Test 1: Current Session Stats");
        SessionStats stats = sessionService.getSessionStats(userId);
        System.out.println(stats);
        System.out.println();

        System.out.println("---\n");

        // Test 2: Get new questions (never reviewed)
        System.out.println("📚 Test 2: Get New Questions (never reviewed)");
        List<Question> newQuestions = sessionService.getNewQuestions(userId, 3);
        System.out.println("Found " + newQuestions.size() + " new questions:");
        for (int i = 0; i < newQuestions.size(); i++) {
            Question q = newQuestions.get(i);
            System.out.println((i + 1) + ". [ID:" + q.getId() + "] " + q.getQuestionText());
        }
        System.out.println();

        System.out.println("---\n");

        // Test 3: Interactive study session
        if (!newQuestions.isEmpty()) {
            System.out.println("🎯 Test 3: Interactive Study Session");
            System.out.println("Let's study the first new question!\n");

            Question firstQuestion = newQuestions.get(0);
            System.out.println("Question: " + firstQuestion.getQuestionText());
            System.out.println("\nThink about the answer...");
            System.out.println("Actual Answer: " + firstQuestion.getAnswer());

            System.out.print("\nDid you get it correct? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            boolean wasCorrect = response.equals("y") || response.equals("yes");

            // Submit the answer
            boolean saved = sessionService.submitAnswer(firstQuestion.getId(), userId, wasCorrect);

            if (saved) {
                System.out.println("\n✅ Review saved!");
                if (wasCorrect) {
                    System.out.println("Great job! You'll see this question again tomorrow.");
                } else {
                    System.out.println("No worries! You'll see this question again tomorrow to practice.");
                }
            } else {
                System.out.println("\n❌ Failed to save review!");
            }

            System.out.println();
        }

        System.out.println("---\n");

        // Test 4: Check due questions
        System.out.println("⏰ Test 4: Questions Due for Review Today");
        List<Question> dueQuestions = sessionService.getDueQuestions(userId);
        System.out.println("Questions due today: " + dueQuestions.size());
        if (!dueQuestions.isEmpty()) {
            System.out.println("Due questions:");
            for (Question q : dueQuestions) {
                System.out.println("  - [ID:" + q.getId() + "] " +
                        q.getQuestionText().substring(0, Math.min(50, q.getQuestionText().length())) + "...");
            }
        } else {
            System.out.println("No questions due today! 🎉");
        }
        System.out.println();

        System.out.println("---\n");

        // Test 5: Updated stats
        System.out.println("📊 Test 5: Updated Session Stats");
        SessionStats updatedStats = sessionService.getSessionStats(userId);
        System.out.println(updatedStats);
        System.out.println();

        System.out.println("---\n");

        // Test 6: Simulate multiple reviews
        System.out.println("🔥 Test 6: Simulate Multiple Reviews");
        System.out.println("Let's simulate reviewing 3 questions...\n");

        List<Question> questionsToReview = sessionService.getNewQuestions(userId, 3);
        int simulatedCorrect = 0;

        for (int i = 0; i < Math.min(3, questionsToReview.size()); i++) {
            Question q = questionsToReview.get(i);

            // Simulate: 70% chance of correct answer
            boolean correct = Math.random() < 0.7;

            System.out.println("Question " + (i + 1) + ": " + q.getQuestionText().substring(0, Math.min(40, q.getQuestionText().length())) + "...");
            System.out.println("Result: " + (correct ? "✅ CORRECT" : "❌ WRONG"));

            sessionService.submitAnswer(q.getId(), userId, correct);

            if (correct) simulatedCorrect++;
        }

        System.out.println("\nSimulation complete!");
        System.out.println("Correct: " + simulatedCorrect + " / " + Math.min(3, questionsToReview.size()));
        System.out.println();

        System.out.println("---\n");

        // Final stats
        System.out.println("📊 Final Session Stats");
        SessionStats finalStats = sessionService.getSessionStats(userId);
        System.out.println(finalStats);

        System.out.println("\n=== TEST COMPLETE ===");
        System.out.println("\n💡 What just happened:");
        System.out.println("- Found new questions you've never reviewed");
        System.out.println("- Submitted answers and calculated next review dates");
        System.out.println("- Tracked your performance statistics");
        System.out.println("- This is the CORE of your study app! 🎯");

        scanner.close();
    }
}