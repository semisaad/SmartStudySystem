package com.semisaad.smartstudy;

import com.semisaad.smartstudy.dao.QuestionDAO;
import com.semisaad.smartstudy.dao.TopicDAO;
import com.semisaad.smartstudy.model.Question;
import com.semisaad.smartstudy.model.Topic;

import java.util.List;

public class QuestionDAOTest {
    public static void main(String[] args) {
        QuestionDAO questionDAO = new QuestionDAO();
        TopicDAO topicDAO = new TopicDAO();

        System.out.println("=== COMPLETE QuestionDAO TEST ===\n");

        // Test 1: Get count
        System.out.println("📊 Test 1: Get Question Count");
        int count = questionDAO.getCount();
        System.out.println("Total questions in database: " + count);

        System.out.println("\n---\n");

        // Test 2: Get all questions
        System.out.println("📋 Test 2: Get All Questions");
        List<Question> allQuestions = questionDAO.getAll();
        System.out.println("Found " + allQuestions.size() + " questions:");
        for (Question q : allQuestions) {
            System.out.println("  - [" + q.getId() + "] " + q.getQuestionText().substring(0, Math.min(50, q.getQuestionText().length())) + "...");
        }

        System.out.println("\n---\n");

        // Test 3: Insert new question
        System.out.println("➕ Test 3: Insert New Question");

        // Get a topic first (Data Structures = ID 1)
        Topic topic = topicDAO.getById(1);
        if (topic != null) {
            Question newQuestion = new Question(
                    "What is the time complexity of searching in a balanced Binary Search Tree?",
                    "O(log n) - because we eliminate half the tree at each step",
                    topic.getId(),
                    "MEDIUM"
            );

            boolean inserted = questionDAO.insert(newQuestion);
            System.out.println(inserted ? "✅ Question inserted successfully!" : "❌ Insert failed!");
        }

        System.out.println("\n---\n");

        // Test 4: Get by topic
        System.out.println("🔍 Test 4: Get Questions by Topic (Data Structures)");
        List<Question> dsQuestions = questionDAO.getByTopicId(1);
        System.out.println("Found " + dsQuestions.size() + " Data Structures questions:");
        for (Question q : dsQuestions) {
            System.out.println("  - " + q.getQuestionText().substring(0, Math.min(60, q.getQuestionText().length())) + "...");
        }

        System.out.println("\n---\n");

        // Test 5: Get by difficulty
        System.out.println("🎯 Test 5: Get Questions by Difficulty (MEDIUM)");
        List<Question> mediumQuestions = questionDAO.getByDifficulty("MEDIUM");
        System.out.println("Found " + mediumQuestions.size() + " MEDIUM difficulty questions");

        System.out.println("\n---\n");

        // Test 6: Get by ID
        System.out.println("🔎 Test 6: Get Question by ID (ID = 1)");
        Question question = questionDAO.getById(1);
        if (question != null) {
            System.out.println("Question: " + question.getQuestionText());
            System.out.println("Answer: " + question.getAnswer());
            System.out.println("Difficulty: " + question.getDifficulty());
        }

        System.out.println("\n---\n");

        // Test 7: Update question
        System.out.println("✏️ Test 7: Update Question");
        if (question != null) {
            question.setAnswer("UPDATED: " + question.getAnswer());
            boolean updated = questionDAO.update(question);
            System.out.println(updated ? "✅ Update successful!" : "❌ Update failed!");

            Question updatedQuestion = questionDAO.getById(1);
            System.out.println("New answer: " + updatedQuestion.getAnswer());
        }

        System.out.println("\n---\n");

        // Test 8: Delete question
        System.out.println("🗑️ Test 8: Delete Question");
        System.out.println("⚠️  Searching for the test question we inserted...");

        allQuestions = questionDAO.getAll();
        Question testQuestion = null;
        for (Question q : allQuestions) {
            if (q.getQuestionText().contains("time complexity of searching in a balanced Binary Search Tree")) {
                testQuestion = q;
                break;
            }
        }

        if (testQuestion != null) {
            System.out.println("Found test question with ID: " + testQuestion.getId());
            boolean deleted = questionDAO.delete(testQuestion.getId());
            System.out.println(deleted ? "✅ Delete successful!" : "❌ Delete failed!");

            int newCount = questionDAO.getCount();
            System.out.println("Questions remaining: " + newCount);
        } else {
            System.out.println("Test question not found (may have been deleted already)");
        }

        System.out.println("\n=== TEST COMPLETE ===");
    }
}
