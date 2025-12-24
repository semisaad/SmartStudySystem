package com.semisaad.smartstudy;

import com.semisaad.smartstudy.dao.TopicDAO;
import com.semisaad.smartstudy.model.Topic;
import java.util.List;

public class TopicDAOTest {
    public static void main(String[] args) {
        TopicDAO dao = new TopicDAO();

        System.out.println("=== COMPLETE TopicDAO TEST ===\n");

        // Test 1: Get all topics
        System.out.println("📋 Test 1: Get All Topics");
        List<Topic> allTopics = dao.getAll();
        System.out.println("Found " + allTopics.size() + " topics");
        for (Topic t : allTopics) {
            System.out.println("  - [" + t.getId() + "] " + t.getName());
        }

        System.out.println("\n---\n");

        // Test 2: Insert new topic
        System.out.println("➕ Test 2: Insert New Topic");
        Topic newTopic = new Topic("Test Topic", "A topic for testing CRUD operations");
        boolean inserted = dao.insert(newTopic);
        System.out.println(inserted ? "✅ Insert successful!" : "❌ Insert failed!");

        System.out.println("\n---\n");

        // Test 3: Get by ID
        System.out.println("🔍 Test 3: Get Topic by ID (ID = 1)");
        Topic topic = dao.getById(1);
        if (topic != null) {
            System.out.println("Found: " + topic.getName());
            System.out.println("Description: " + topic.getDescription());
        }

        System.out.println("\n---\n");

        // Test 4: Update topic
        System.out.println("✏️ Test 4: Update Topic");
        if (topic != null) {
            topic.setDescription("UPDATED: " + topic.getDescription());
            boolean updated = dao.update(topic);
            System.out.println(updated ? "✅ Update successful!" : "❌ Update failed!");

            // Read it back to verify
            Topic updatedTopic = dao.getById(1);
            System.out.println("New description: " + updatedTopic.getDescription());
        }

        System.out.println("\n---\n");

        // Test 5: Delete topic
        System.out.println("🗑️ Test 5: Delete Topic");
        System.out.println("⚠️  WARNING: This will delete the 'Test Topic' we just created!");
        System.out.println("Searching for 'Test Topic'...");

        // Find the test topic we created
        allTopics = dao.getAll();
        Topic testTopic = null;
        for (Topic t : allTopics) {
            if (t.getName().equals("Test Topic")) {
                testTopic = t;
                break;
            }
        }

        if (testTopic != null) {
            System.out.println("Found 'Test Topic' with ID: " + testTopic.getId());
            boolean deleted = dao.delete(testTopic.getId());
            System.out.println(deleted ? "✅ Delete successful!" : "❌ Delete failed!");

            // Verify it's gone
            allTopics = dao.getAll();
            System.out.println("Topics remaining: " + allTopics.size());
        } else {
            System.out.println("Test Topic not found (might have been deleted already)");
        }

        System.out.println("\n=== TEST COMPLETE ===");
    }
}

