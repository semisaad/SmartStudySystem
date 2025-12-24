package com.semisaad.smartstudy.dao;

import com.semisaad.smartstudy.database.DatabaseConnection;
import com.semisaad.smartstudy.model.Topic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TopicDAO {
    /**
     * Insert a new topic into the database
     * @param topic The topic to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(Topic topic) {
        String sql = "INSERT INTO topics (name, description, created_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the values for the ? placeholders
            pstmt.setString(1, topic.getName());                    // First ? = name
            pstmt.setString(2, topic.getDescription());             // Second ? = description
            pstmt.setTimestamp(3, Timestamp.valueOf(topic.getCreatedAt())); // Third ? = created_at

            // Execute the insert
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0; // Returns true if at least 1 row was inserted

        } catch (SQLException e) {
            System.err.println("Error inserting topic: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all topics from the database
     * @return List of all topics
     */
    public List<Topic> getAll() {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT * FROM topics ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Loop through each row in the result
            while (rs.next()) {
                // Extract data from this row
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                // Create a Topic object from this row
                Topic topic = new Topic(id, name, description, createdAt);

                // Add it to our list
                topics.add(topic);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all topics: " + e.getMessage());
        }

        return topics; // Return the list (might be empty if error or no data)
    }

    /**
     * Get a topic by its ID
     * @param id The topic ID
     * @return Topic object, or null if not found
     */
    public Topic getById(int id) {
        String sql = "SELECT * FROM topics WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the ID parameter
            pstmt.setInt(1, id);

            // Execute query
            ResultSet rs = pstmt.executeQuery();

            // Check if we found a topic
            if (rs.next()) {
                // Extract data
                String name = rs.getString("name");
                String description = rs.getString("description");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                // Create and return Topic
                return new Topic(id, name, description, createdAt);
            }

            // Close ResultSet
            rs.close();

        } catch (SQLException e) {
            System.err.println("Error getting topic by ID: " + e.getMessage());
        }

        return null; // Return null if not found or error
    }

    /**
     * Update an existing topic
     * @param topic The topic with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(Topic topic) {
        String sql = "UPDATE topics SET name = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameters
            pstmt.setString(1, topic.getName());        // First ? = name
            pstmt.setString(2, topic.getDescription()); // Second ? = description
            pstmt.setInt(3, topic.getId());             // Third ? = id (which topic to update)

            // Execute the update
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating topic: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a topic by its ID
     * @param id The topic ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM topics WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the ID parameter
            pstmt.setInt(1, id);

            // Execute the delete
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting topic: " + e.getMessage());
            return false;
        }
    }


}