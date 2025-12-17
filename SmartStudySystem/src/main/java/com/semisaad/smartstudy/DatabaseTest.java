package com.semisaad.smartstudy;

import com.semisaad.smartstudy.database.DatabaseConnection;
import java.sql.Connection;

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");

        if (DatabaseConnection.testConnection()) {
            System.out.println("✅ Database connected successfully!");
        } else {
            System.out.println("❌ Connection failed!");
        }
    }
}