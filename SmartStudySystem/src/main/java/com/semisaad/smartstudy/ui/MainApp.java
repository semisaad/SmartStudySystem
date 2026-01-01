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

    private BorderPane mainLayout;
    private StackPane contentArea;
    private StudySessionService studyService;
    private TopicDAO topicDAO;
    private QuestionDAO questionDAO;
    private int currentUserId = 1; // Using user ID 1 (saad)

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
        VBox sidebar = createSidebar();
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
        homeBtn.setOnAction(e -> showDashboard());

        Button topicsBtn = createNavButton("📁  Topics", false);
        topicsBtn.setOnAction(e -> showTopics());

        Button questionsBtn = createNavButton("📄  Questions", false);
        questionsBtn.setOnAction(e -> showQuestions());

        Button studyBtn = createNavButton("🎯  Study Session", false);
        studyBtn.setOnAction(e -> showStudySession());

        Button statsBtn = createNavButton("📊  Statistics", false);
        statsBtn.setOnAction(e -> showStatistics());

        Button settingsBtn = createNavButton("⚙️  Settings", false);
        settingsBtn.setOnAction(e -> showSettings());

        navMenu.getChildren().addAll(homeBtn, topicsBtn, questionsBtn, studyBtn, statsBtn, settingsBtn);

        sidebar.getChildren().addAll(storageCard, navMenu);

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

        // Stats cards
        HBox statsRow = new HBox(20);

        var stats = studyService.getSessionStats(currentUserId);

        VBox dueCard = createStatCard("Due Today", String.valueOf(stats.getQuestionsDueToday()), "+3 from yesterday");
        VBox successCard = createStatCard("Success Rate", String.format("%.0f%%", stats.getSuccessRate()), "+5% this week");
        VBox totalCard = createStatCard("Total Reviews", String.valueOf(stats.getTotalReviews()), "Keep going! 🔥");

        statsRow.getChildren().addAll(dueCard, successCard, totalCard);

        // Topics section
        HBox topicsHeader = new HBox();
        topicsHeader.setAlignment(Pos.CENTER_LEFT);
        Label topicsTitle = new Label("Topics");
        topicsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        topicsHeader.getChildren().add(topicsTitle);

        // Topics grid
        FlowPane topicsGrid = new FlowPane(20, 20);

        List<Topic> topics = topicDAO.getAll();
        String[] colors = {"yellow", "blue", "purple", "cyan", "pink", "green"};
        String[] icons = {"📦", "🔄", "🎯", "🗄️", "💻", "🌐"};

        for (int i = 0; i < Math.min(topics.size(), 6); i++) {
            Topic topic = topics.get(i);
            int questionCount = questionDAO.getByTopicId(topic.getId()).size();
            VBox topicCard = createTopicCard(topic.getName(), questionCount + " questions",
                    icons[i % icons.length], colors[i % colors.length]);
            topicsGrid.getChildren().add(topicCard);
        }

        // Recent questions
        HBox questionsHeader = new HBox();
        questionsHeader.setAlignment(Pos.CENTER_LEFT);
        Label questionsTitle = new Label("Recent Questions");
        questionsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        questionsHeader.getChildren().add(questionsTitle);

        VBox questionsTable = createQuestionsTable();

        dashboard.getChildren().addAll(header, statsRow, topicsHeader, topicsGrid, questionsHeader, questionsTable);

        ScrollPane scrollPane = new ScrollPane(dashboard);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
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

    private VBox createQuestionsTable() {
        VBox table = new VBox();
        table.getStyleClass().add("questions-table");

        // Table header
        HBox header = new HBox();
        header.getStyleClass().add("table-header");
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setSpacing(20);

        Label nameHeader = new Label("Name");
        nameHeader.setPrefWidth(400);
        Label topicHeader = new Label("Topic");
        topicHeader.setPrefWidth(200);
        Label reviewHeader = new Label("Next Review");
        reviewHeader.setPrefWidth(150);

        header.getChildren().addAll(new Label(""), nameHeader, topicHeader, reviewHeader);

        // Table rows
        VBox rows = new VBox();

        List<Question> questions = questionDAO.getAll();
        for (int i = 0; i < Math.min(questions.size(), 5); i++) {
            Question q = questions.get(i);
            Topic topic = topicDAO.getById(q.getTopicId());

            HBox row = new HBox();
            row.getStyleClass().add("table-row");
            row.setPadding(new Insets(15, 20, 15, 20));
            row.setSpacing(20);

            Label icon = new Label("📝");
            icon.setFont(Font.font(18));

            Label name = new Label(q.getQuestionText());
            name.setPrefWidth(400);
            name.setWrapText(true);

            Label topicName = new Label(topic != null ? topic.getName() : "Unknown");
            topicName.setPrefWidth(200);

            Label nextReview = new Label("Tomorrow");
            nextReview.setPrefWidth(150);

            row.getChildren().addAll(icon, name, topicName, nextReview);
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
        Label label = new Label("Study Session - Coming Soon!");
        label.setFont(Font.font(24));

        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
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
}
