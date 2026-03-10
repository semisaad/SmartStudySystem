package com.semisaad.smartstudy.util;

public class Validator {

    /**
     * Validate topic name
     */
    public static ValidationResult validateTopicName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Topic name cannot be empty");
        }

        if (name.length() > 50) {
            return new ValidationResult(false, "Topic name must be 50 characters or less");
        }

        if (!name.matches("^[a-zA-Z0-9\\s\\-_]+$")) {
            return new ValidationResult(false, "Topic name can only contain letters, numbers, spaces, hyphens, and underscores");
        }

        return new ValidationResult(true, "Valid");
    }

    /**
     * Validate question text
     */
    public static ValidationResult validateQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return new ValidationResult(false, "Question cannot be empty");
        }

        if (question.length() < 10) {
            return new ValidationResult(false, "Question must be at least 10 characters");
        }

        if (question.length() > 500) {
            return new ValidationResult(false, "Question must be 500 characters or less");
        }

        return new ValidationResult(true, "Valid");
    }

    /**
     * Validate answer text
     */
    public static ValidationResult validateAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return new ValidationResult(false, "Answer cannot be empty");
        }

        if (answer.length() < 5) {
            return new ValidationResult(false, "Answer must be at least 5 characters");
        }

        if (answer.length() > 1000) {
            return new ValidationResult(false, "Answer must be 1000 characters or less");
        }

        return new ValidationResult(true, "Valid");
    }

    /**
     * Sanitize input to prevent SQL injection
     */
    public static String sanitize(String input) {
        if (input == null) return "";

        // Remove potentially dangerous characters
        return input.replaceAll("[;'\"\\\\]", "");
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}