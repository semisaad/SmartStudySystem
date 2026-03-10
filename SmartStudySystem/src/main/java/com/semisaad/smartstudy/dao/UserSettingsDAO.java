package com.semisaad.smartstudy.dao;

import com.semisaad.smartstudy.model.UserSettings;
import com.semisaad.smartstudy.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class UserSettingsDAO {

    /**
     * Get user settings by user ID
     */
    public UserSettings getByUserId(int userId) {
        String sql = "SELECT * FROM user_settings WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserSettings settings = new UserSettings();
                settings.setUserId(rs.getInt("user_id"));
                settings.setDailyGoal(rs.getInt("daily_goal"));
                settings.setRemindersEnabled(rs.getBoolean("reminders_enabled"));
                settings.setTheme(rs.getString("theme"));
                settings.setNotificationTime(rs.getString("notification_time"));

                Timestamp createdTs = rs.getTimestamp("created_at");
                Timestamp updatedTs = rs.getTimestamp("updated_at");

                if (createdTs != null) {
                    settings.setCreatedAt(createdTs.toLocalDateTime());
                }
                if (updatedTs != null) {
                    settings.setUpdatedAt(updatedTs.toLocalDateTime());
                }

                return settings;
            }

        } catch (SQLException e) {
            System.err.println("Error getting user settings: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Insert new user settings
     */
    public boolean insert(UserSettings settings) {
        String sql = "INSERT INTO user_settings (user_id, daily_goal, reminders_enabled, theme, notification_time, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, settings.getUserId());
            stmt.setInt(2, settings.getDailyGoal());
            stmt.setBoolean(3, settings.isRemindersEnabled());
            stmt.setString(4, settings.getTheme());
            stmt.setString(5, settings.getNotificationTime());
            stmt.setTimestamp(6, Timestamp.valueOf(settings.getCreatedAt()));
            stmt.setTimestamp(7, Timestamp.valueOf(settings.getUpdatedAt()));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting user settings: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update existing user settings
     */
    public boolean update(UserSettings settings) {
        String sql = "UPDATE user_settings SET daily_goal = ?, reminders_enabled = ?, theme = ?, " +
                "notification_time = ?, updated_at = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, settings.getDailyGoal());
            stmt.setBoolean(2, settings.isRemindersEnabled());
            stmt.setString(3, settings.getTheme());
            stmt.setString(4, settings.getNotificationTime());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(6, settings.getUserId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user settings: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create or update user settings (upsert)
     */
    public boolean save(UserSettings settings) {
        UserSettings existing = getByUserId(settings.getUserId());

        if (existing == null) {
            return insert(settings);
        } else {
            return update(settings);
        }
    }

    /**
     * Delete user settings
     */
    public boolean delete(int userId) {
        String sql = "DELETE FROM user_settings WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user settings: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Initialize default settings for a user if they don't exist
     */
    public UserSettings getOrCreateDefault(int userId) {
        UserSettings settings = getByUserId(userId);

        if (settings == null) {
            // Create default settings
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setDailyGoal(10);
            settings.setRemindersEnabled(true);
            settings.setTheme("light");
            settings.setNotificationTime("09:00");

            if (insert(settings)) {
                return settings;
            } else {
                // Return default object even if insert fails
                return settings;
            }
        }

        return settings;
    }
}