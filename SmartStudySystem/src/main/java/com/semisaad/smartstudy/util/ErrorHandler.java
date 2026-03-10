package com.semisaad.smartstudy.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.control.ButtonBar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ErrorHandler {

    private static final String LOG_FILE = "error_log.txt";

    /**
     * Show user-friendly error dialog
     */
    public static void showError(String title, String message, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText("An error occurred. Please try again.");

        // Add details button for technical info
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(expContent);

            // Log error
            logError(message, e);
        }

        alert.showAndWait();
    }

    /**
     * Show warning dialog with retry option
     */
    public static boolean showRetryDialog(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText("Would you like to try again?");

        ButtonType retryButton = new ButtonType("Retry");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(retryButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == retryButton;
    }

    /**
     * Show connection error with helpful message
     */
    public static void showConnectionError() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText("Cannot connect to database");
        alert.setContentText(
                "Please check:\n" +
                        "• PostgreSQL is running\n" +
                        "• Database credentials are correct\n" +
                        "• Network connection is stable\n\n" +
                        "Check database settings in config file."
        );
        alert.showAndWait();
    }

    /**
     * Log error to file
     */
    private static void logError(String message, Exception e) {
        try (PrintWriter out = new PrintWriter(
                new java.io.FileWriter(LOG_FILE, true))) {

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            out.println("=== ERROR " + timestamp + " ===");
            out.println("Message: " + message);
            if (e != null) {
                out.println("Exception: " + e.getClass().getName());
                out.println("Error: " + e.getMessage());
                e.printStackTrace(out);
            }
            out.println();

        } catch (Exception logError) {
            System.err.println("Failed to write to error log: " + logError.getMessage());
        }
    }

    /**
     * Show loading indicator
     */
    public static Alert showLoadingDialog(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Please Wait");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().clear();
        return alert;
    }
}