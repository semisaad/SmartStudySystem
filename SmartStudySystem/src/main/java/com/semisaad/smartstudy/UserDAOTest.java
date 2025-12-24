package com.semisaad.smartstudy;

import com.semisaad.smartstudy.dao.UserDAO;
import com.semisaad.smartstudy.model.User;

import java.util.List;

public class UserDAOTest {
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();

        System.out.println("=== COMPLETE UserDAO TEST ===\n");

        // Test 1: Get count
        System.out.println("📊 Test 1: Get User Count");
        int count = dao.getCount();
        System.out.println("Total users in database: " + count);

        System.out.println("\n---\n");

        // Test 2: Get all users
        System.out.println("📋 Test 2: Get All Users");
        List<User> allUsers = dao.getAll();
        System.out.println("Found " + allUsers.size() + " users:");
        for (User u : allUsers) {
            System.out.println("  - [" + u.getId() + "] " + u.getUsername() + " (" + u.getEmail() + ")");
        }

        System.out.println("\n---\n");

        // Test 3: Insert new user
        System.out.println("➕ Test 3: Insert New User");
        User newUser = new User("testuser", "testuser@example.com");
        boolean inserted = dao.insert(newUser);
        System.out.println(inserted ? "✅ User inserted successfully!" : "❌ Insert failed!");

        System.out.println("\n---\n");

        // Test 4: Get by ID
        System.out.println("🔍 Test 4: Get User by ID (ID = 1)");
        User user = dao.getById(1);
        if (user != null) {
            System.out.println("Found: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Created: " + user.getCreatedAt());
        } else {
            System.out.println("User with ID 1 not found!");
        }

        System.out.println("\n---\n");

        // Test 5: Get by username
        System.out.println("🔎 Test 5: Get User by Username");
        User saadUser = dao.getByUsername("saad");
        if (saadUser != null) {
            System.out.println("Found user 'saad': ID = " + saadUser.getId());
        } else {
            System.out.println("User 'saad' not found!");
        }

        System.out.println("\n---\n");

        // Test 6: Update user
        System.out.println("✏️ Test 6: Update User");
        if (user != null) {
            user.setEmail("updated_" + user.getEmail());
            boolean updated = dao.update(user);
            System.out.println(updated ? "✅ Update successful!" : "❌ Update failed!");

            User updatedUser = dao.getById(1);
            System.out.println("New email: " + updatedUser.getEmail());
        }

        System.out.println("\n---\n");

        // Test 7: Delete user
        System.out.println("🗑️ Test 7: Delete User");
        System.out.println("⚠️  Searching for 'testuser' to delete...");

        User testUser = dao.getByUsername("testuser");
        if (testUser != null) {
            System.out.println("Found 'testuser' with ID: " + testUser.getId());
            boolean deleted = dao.delete(testUser.getId());
            System.out.println(deleted ? "✅ Delete successful!" : "❌ Delete failed!");

            int newCount = dao.getCount();
            System.out.println("Users remaining: " + newCount);
        } else {
            System.out.println("User 'testuser' not found (may have been deleted already)");
        }

        System.out.println("\n=== TEST COMPLETE ===");
    }
}