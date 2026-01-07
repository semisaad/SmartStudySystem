package com.semisaad.smartstudy.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import com.semisaad.smartstudy.service.StudySessionService;
import com.semisaad.smartstudy.dao.TopicDAO;
import com.semisaad.smartstudy.dao.QuestionDAO;
import com.semisaad.smartstudy.model.Topic;
import com.semisaad.smartstudy.model.Question;

import java.util.List;

public class MainApp extends Application {

    private VBox sidebar;
    private Button activeNavButton;
    private BorderPane mainLayout;
    private StackPane contentArea;
    private StudySessionService studyService;
    private TopicDAO topicDAO;
    private QuestionDAO questionDAO;
    private int currentUserId = 1; // Using user ID 1 (saad)
    private List<Question> currentStudyQuestions;
    private int currentQuestionIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        // Initialize services
        studyService = new StudySessionService();
        topicDAO = new TopicDAO();
        questionDAO = new QuestionDAO();

        // Create main layout
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-container");

        // Create sidebar
        sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Create content area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        mainLayout.setCenter(contentArea);

        // Show dashboard by default
        showDashboard();

        // Create scene
        Scene scene = new Scene(mainLayout, 1400, 900);

        // Load CSS
        try {
            String css = getClass().getResource("/styles/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("CSS file not found, using default styles");
        }

        // Configure stage
        primaryStage.setTitle("Smart Study System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);
        sidebar.setPadding(new Insets(30, 20, 30, 20));

        // Storage info card
        VBox storageCard = new VBox(8);
        storageCard.getStyleClass().add("storage-card");
        storageCard.setPadding(new Insets(20));

        Label storageLabel = new Label("Study Progress");
        storageLabel.getStyleClass().add("storage-label");

        int totalQuestions = questionDAO.getCount();
        int reviewedQuestions = studyService.getSessionStats(currentUserId).getTotalReviews();

        Label storageText = new Label(reviewedQuestions + " / " + totalQuestions + " Questions");
        storageText.setFont(Font.font("System", FontWeight.BOLD, 18));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress((double) reviewedQuestions / totalQuestions);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("storage-progress");

        storageCard.getChildren().addAll(storageLabel, storageText, progressBar);

        // Navigation menu
        VBox navMenu = new VBox(4);
        navMenu.setPadding(new Insets(20, 0, 0, 0));

        Button homeBtn = createNavButton("🏠  Home", true);
        homeBtn.setOnAction(e -> {
            setActiveNavButton(homeBtn);
            showDashboard();
        });

        Button topicsBtn = createNavButton("📁  Topics", false);
        topicsBtn.setOnAction(e -> {
            setActiveNavButton(topicsBtn);
            showTopics();
        });

        Button questionsBtn = createNavButton("📄  Questions", false);
        questionsBtn.setOnAction(e -> {
            setActiveNavButton(questionsBtn);
            showQuestions();
        });

        Button studyBtn = createNavButton("🎯  Study Session", false);
        studyBtn.setOnAction(e -> {
            setActiveNavButton(studyBtn);
            showStudySession();
        });

        Button statsBtn = createNavButton("📊  Statistics", false);
        statsBtn.setOnAction(e -> {
            setActiveNavButton(statsBtn);
            showStatistics();
        });

        Button settingsBtn = createNavButton("⚙️  Settings", false);
        settingsBtn.setOnAction(e -> {
            setActiveNavButton(settingsBtn);
            showSettings();
        });

        navMenu.getChildren().addAll(homeBtn, topicsBtn, questionsBtn, studyBtn, statsBtn, settingsBtn);

        // Set home button as initially active
        activeNavButton = homeBtn;sidebar.getChildren().addAll(storageCard, navMenu);

        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        if (active) {
            btn.getStyleClass().add("nav-button-active");
        }
        btn.setPrefWidth(220);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void showDashboard() {
        VBox dashboard = new VBox(30);
        dashboard.setPadding(new Insets(30, 40, 30, 40));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(5);
        Label title = new Label("Home");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        Label welcome = new Label("👋 Hello Saad, welcome!");
        welcome.getStyleClass().add("welcome-text");
        headerText.getChildren().addAll(title, welcome);

        header.getChildren().add(headerText);

        // Stats cards - fetch once
        var stats = studyService.getSessionStats(currentUserId);

        HBox statsRow = new HBox(20);
        VBox dueCard = createStatCard("Due Today", String.valueOf(stats.getQuestionsDueToday()), "+3 from yesterday");
        VBox successCard = createStatCard("Success Rate", String.format("%.0f%%", stats.getSuccessRate()), "+5% this week");
        VBox totalCard = createStatCard("Total Reviews", String.valueOf(stats.getTotalReviews()), "Keep going! 🔥");

        statsRow.getChildren().addAll(dueCard, successCard, totalCard);

        // Topics section header
        HBox topicsHeader = new HBox();
        topicsHeader.setAlignment(Pos.CENTER_LEFT);
        Label topicsTitle = new Label("Topics");
        topicsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        topicsHeader.getChildren().add(topicsTitle);

        // Topics grid - simplified
        FlowPane topicsGrid = new FlowPane(20, 20);

        // Fetch topics once
        List<Topic> topics = topicDAO.getAll();
        String[] colors = {"yellow", "blue", "purple", "cyan", "pink", "green"};
        String[] icons = {"📦", "🔄", "🎯", "🗄️", "💻", "🌐"};

        // Limit to 4 topics for speed
        for (int i = 0; i < Math.min(topics.size(), 4); i++) {
            Topic topic = topics.get(i);
            // Get count asynchronously or cache it
            int questionCount = questionDAO.getByTopicId(topic.getId()).size();
            VBox topicCard = createTopicCard(topic.getName(), questionCount + " questions",
                    icons[i % icons.length], colors[i % colors.length]);
            topicsGrid.getChildren().add(topicCard);
        }

        // Recent questions header
        HBox questionsHeader = new HBox();
        questionsHeader.setAlignment(Pos.CENTER_LEFT);
        Label questionsTitle = new Label("Recent Questions");
        questionsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        questionsHeader.getChildren().add(questionsTitle);

        // Simplified questions table
        VBox questionsTable = createSimpleQuestionsTable();

        dashboard.getChildren().addAll(header, statsRow, topicsHeader, topicsGrid, questionsHeader, questionsTable);

        // NO ScrollPane - just show content directly for speed
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboard);
    }

    private VBox createStatCard(String label, String value, String change) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);

        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-label");

        Label valueText = new Label(value);
        valueText.setFont(Font.font("System", FontWeight.BOLD, 32));

        Label changeText = new Label(change);
        changeText.getStyleClass().add("stat-change");

        card.getChildren().addAll(labelText, valueText, changeText);

        return card;
    }

    private VBox createTopicCard(String name, String count, String icon, String color) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("topic-card", "topic-card-" + color);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(150);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));
        iconLabel.getStyleClass().add("topic-icon");

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setWrapText(true);

        Label countLabel = new Label(count);
        countLabel.getStyleClass().add("topic-count");

        card.getChildren().addAll(iconLabel, nameLabel, countLabel);

        return card;
    }

    private VBox createSimpleQuestionsTable() {
        VBox table = new VBox();
        table.getStyleClass().add("questions-table");
        table.setMaxHeight(300); // Limit height

        // Table header
        HBox header = new HBox();
        header.getStyleClass().add("table-header");
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setSpacing(20);

        Label nameHeader = new Label("Name");
        nameHeader.setPrefWidth(400);
        Label topicHeader = new Label("Topic");
        topicHeader.setPrefWidth(200);

        header.getChildren().addAll(new Label(""), nameHeader, topicHeader);

        // Table rows - ONLY 3 for speed
        VBox rows = new VBox();

        List<Question> questions = questionDAO.getAll();
        for (int i = 0; i < Math.min(questions.size(), 3); i++) {
            Question q = questions.get(i);
            Topic topic = topicDAO.getById(q.getTopicId());

            HBox row = new HBox();
            row.getStyleClass().add("table-row");
            row.setPadding(new Insets(15, 20, 15, 20));
            row.setSpacing(20);

            Label icon = new Label("📝");
            icon.setFont(Font.font(18));

            // Truncate long questions
            String questionText = q.getQuestionText();
            if (questionText.length() > 50) {
                questionText = questionText.substring(0, 50) + "...";
            }
            Label name = new Label(questionText);
            name.setPrefWidth(400);

            Label topicName = new Label(topic != null ? topic.getName() : "Unknown");
            topicName.setPrefWidth(200);

            row.getChildren().addAll(icon, name, topicName);
            rows.getChildren().add(row);
        }

        table.getChildren().addAll(header, rows);

        return table;
    }

    private void showTopics() {
        Label label = new Label("Topics Screen - Coming Soon!");
        label.setFont(Font.font(24));

        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }

    private void showQuestions() {
        Label label = new Label("Questions Screen - Coming Soon!");
        label.setFont(Font.font(24));

        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }

    private void showStudySession() {
        // Get questions due today
        currentStudyQuestions = studyService.getDueQuestions(currentUserId);

        // If no questions, get some new ones
        if (currentStudyQuestions.isEmpty()) {
            currentStudyQuestions = studyService.getNewQuestions(currentUserId, 10);
        }

        // Reset index
        currentQuestionIndex = 0;

        // Check if we have questions
        if (currentStudyQuestions.isEmpty()) {
            showNoQuestionsScreen();
            return;
        }

        // Show first question
        showQuestionCard();
    }

    private void showNoQuestionsScreen() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(100));

        Label icon = new Label("🎉");
        icon.setFont(Font.font(80));

        Label title = new Label("All Caught Up!");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));

        Label message = new Label("You have no questions due for review today.");
        message.setFont(Font.font(16));
        message.setStyle("-fx-text-fill: #6b7280;");

        Button backBtn = new Button("⬅️ Back to Dashboard");
        backBtn.getStyleClass().add("primary-button");
        backBtn.setOnAction(e -> showDashboard());

        content.getChildren().addAll(icon, title, message, backBtn);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    private void showQuestionCard() {
        Question currentQuestion = currentStudyQuestions.get(currentQuestionIndex);
        Topic topic = topicDAO.getById(currentQuestion.getTopicId());

        VBox questionScreen = new VBox(30);
        questionScreen.setPadding(new Insets(40));
        questionScreen.setAlignment(Pos.TOP_CENTER);
        questionScreen.setStyle("-fx-background-color: white;");

        // Progress indicator
        HBox progressBar = new HBox(10);
        progressBar.setAlignment(Pos.CENTER);
        Label progressText = new Label("Question " + (currentQuestionIndex + 1) + " of " + currentStudyQuestions.size());
        progressText.setFont(Font.font("System", FontWeight.BOLD, 16));
        progressText.setStyle("-fx-text-fill: #3b82f6;");
        progressBar.getChildren().add(progressText);

        // Question card
        VBox questionCard = new VBox(25);
        questionCard.setPadding(new Insets(40));
        questionCard.setMaxWidth(800);
        questionCard.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);"
        );

        // Header with topic and difficulty
        HBox cardHeader = new HBox(15);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label topicLabel = new Label("📁 " + (topic != null ? topic.getName() : "Unknown"));
        topicLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

        Label difficultyBadge = new Label(currentQuestion.getDifficulty());
        difficultyBadge.setPadding(new Insets(5, 12, 5, 12));
        difficultyBadge.setStyle(
                "-fx-background-radius: 15; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px; " +
                        getDifficultyColor(currentQuestion.getDifficulty())
        );

        cardHeader.getChildren().addAll(topicLabel, difficultyBadge);

        // Question text
        Label questionText = new Label(currentQuestion.getQuestionText());
        questionText.setWrapText(true);
        questionText.setFont(Font.font("System", FontWeight.NORMAL, 22));
        questionText.setStyle("-fx-text-fill: #1f2937; -fx-line-spacing: 5px;");

        // Answer area (initially hidden)
        VBox answerArea = new VBox(15);
        answerArea.setVisible(false);
        answerArea.setManaged(false);
        answerArea.setPadding(new Insets(20));
        answerArea.setStyle(
                "-fx-background-color: #f0fdf4; " +
                        "-fx-border-color: #86efac; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

        Label answerTitle = new Label("✅ Correct Answer:");
        answerTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        answerTitle.setStyle("-fx-text-fill: #16a34a;");

        Label answerText = new Label(currentQuestion.getAnswer());
        answerText.setWrapText(true);
        answerText.setFont(Font.font(16));
        answerText.setStyle("-fx-text-fill: #1f2937; -fx-line-spacing: 3px;");

        answerArea.getChildren().addAll(answerTitle, answerText);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button showAnswerBtn = new Button("👁️ Show Answer");
        showAnswerBtn.setPrefWidth(200);
        showAnswerBtn.setPrefHeight(45);
        showAnswerBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        Button correctBtn = new Button("✅ I Got It Right");
        correctBtn.setPrefWidth(200);
        correctBtn.setPrefHeight(45);
        correctBtn.setVisible(false);
        correctBtn.setManaged(false);
        correctBtn.setStyle(
                "-fx-background-color: #10b981; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        Button wrongBtn = new Button("❌ I Got It Wrong");
        wrongBtn.setPrefWidth(200);
        wrongBtn.setPrefHeight(45);
        wrongBtn.setVisible(false);
        wrongBtn.setManaged(false);
        wrongBtn.setStyle(
                "-fx-background-color: #ef4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        // Show answer button action
        showAnswerBtn.setOnAction(e -> {
            answerArea.setVisible(true);
            answerArea.setManaged(true);
            showAnswerBtn.setVisible(false);
            showAnswerBtn.setManaged(false);
            correctBtn.setVisible(true);
            correctBtn.setManaged(true);
            wrongBtn.setVisible(true);
            wrongBtn.setManaged(true);
        });

        // Correct button action
        correctBtn.setOnAction(e -> {
            handleAnswer(currentQuestion, true);
        });

        // Wrong button action
        wrongBtn.setOnAction(e -> {
            handleAnswer(currentQuestion, false);
        });

        buttonBox.getChildren().addAll(showAnswerBtn, correctBtn, wrongBtn);

        questionCard.getChildren().addAll(cardHeader, questionText, answerArea, buttonBox);

        questionScreen.getChildren().addAll(progressBar, questionCard);

        ScrollPane scrollPane = new ScrollPane(questionScreen);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private String getDifficultyColor(String difficulty) {
        switch (difficulty.toUpperCase()) {
            case "EASY":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;";
            case "HARD":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;";
            default: // MEDIUM
                return "-fx-background-color: #fed7aa; -fx-text-fill: #ea580c;";
        }
    }

    private void handleAnswer(Question question, boolean wasCorrect) {
        // Submit answer to backend (SM-2 algorithm runs here!)
        boolean saved = studyService.submitAnswer(question.getId(), currentUserId, wasCorrect);

        if (saved) {
            // Show feedback
            showAnswerFeedback(wasCorrect);

            // Move to next question after delay
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> nextQuestion());
            pause.play();
        } else {
            // Show error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save answer");
            alert.setContentText("Please try again.");
            alert.showAndWait();
        }
    }

    private void showAnswerFeedback(boolean wasCorrect) {
        VBox feedback = new VBox(20);
        feedback.setAlignment(Pos.CENTER);
        feedback.setPadding(new Insets(100));
        feedback.setStyle("-fx-background-color: white;");

        Label icon = new Label(wasCorrect ? "🎉" : "💪");
        icon.setFont(Font.font(100));

        Label message = new Label(wasCorrect ? "Great Job!" : "Keep Practicing!");
        message.setFont(Font.font("System", FontWeight.BOLD, 36));
        message.setStyle("-fx-text-fill: " + (wasCorrect ? "#10b981" : "#ef4444") + ";");

        Label detail = new Label(wasCorrect ?
                "You'll see this question again in a few days!" :
                "You'll see this question again tomorrow!");
        detail.setFont(Font.font(18));
        detail.setStyle("-fx-text-fill: #6b7280;");

        feedback.getChildren().addAll(icon, message, detail);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(feedback);
    }

    private void nextQuestion() {
        currentQuestionIndex++;

        if (currentQuestionIndex < currentStudyQuestions.size()) {
            // Show next question
            showQuestionCard();
        } else {
            // Session complete!
            showSessionComplete();
        }
    }

    private void showSessionComplete() {
        VBox complete = new VBox(30);
        complete.setAlignment(Pos.CENTER);
        complete.setPadding(new Insets(100));

        Label icon = new Label("🎊");
        icon.setFont(Font.font(100));

        Label title = new Label("Session Complete!");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));

        Label message = new Label("You reviewed " + currentStudyQuestions.size() + " questions today!");
        message.setFont(Font.font(18));
        message.setStyle("-fx-text-fill: #6b7280;");

        // Get updated stats
        var stats = studyService.getSessionStats(currentUserId);
        Label statsText = new Label(String.format("Success Rate: %.0f%%", stats.getSuccessRate()));
        statsText.setFont(Font.font("System", FontWeight.BOLD, 20));
        statsText.setStyle("-fx-text-fill: #10b981;");

        Button dashboardBtn = new Button("🏠 Back to Dashboard");
        dashboardBtn.setPrefWidth(250);
        dashboardBtn.setPrefHeight(50);
        dashboardBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
        dashboardBtn.setOnAction(e -> {
            setActiveNavButton(activeNavButton); // Refresh to update home button
            showDashboard();
        });

        complete.getChildren().addAll(icon, title, message, statsText, dashboardBtn);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(complete);
    }

    private void showStatistics() {
        Label label = new Label("Statistics - Coming Soon!");
        label.setFont(Font.font(24));

        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }

    private void showSettings() {
        Label label = new Label("Settings - Coming Soon!");
        label.setFont(Font.font(24));

        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void setActiveNavButton(Button button) {
        // Remove active class from previous button
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }

        // Add active class to new button
        button.getStyleClass().add("nav-button-active");

        // Update the active button reference
        activeNavButton = button;
    }
}
