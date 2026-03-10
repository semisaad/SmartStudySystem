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
import javafx.stage.Modality;

import com.semisaad.smartstudy.service.StudySessionService;
import com.semisaad.smartstudy.dao.TopicDAO;
import com.semisaad.smartstudy.dao.QuestionDAO;
import com.semisaad.smartstudy.dao.UserDAO;
import com.semisaad.smartstudy.dao.ReviewDAO;
import com.semisaad.smartstudy.model.Review;
import com.semisaad.smartstudy.model.Topic;
import com.semisaad.smartstudy.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class MainApp extends Application {

    private VBox sidebar;
    private Button activeNavButton;
    private BorderPane mainLayout;
    private StackPane contentArea;
    private StudySessionService studyService;
    private TopicDAO topicDAO;
    private QuestionDAO questionDAO;
    private ReviewDAO reviewDAO;
    private UserDAO userDAO;
    private int currentUserId = 1; // Using user ID 1 (saad)
    private List<Question> currentStudyQuestions;
    private int currentQuestionIndex = 0;
    private Set<Integer> answeredThisSession = new HashSet<>();


    // Settings preferences
    private int dailyGoalQuestions = 10;
    private boolean dailyRemindersEnabled = true;

    @Override
    public void start(Stage primaryStage) {
        // Initialize services
        studyService = new StudySessionService();
        topicDAO = new TopicDAO();
        questionDAO = new QuestionDAO();
        reviewDAO = new ReviewDAO();
        userDAO = new UserDAO();

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

    private int getUniqueReviewedCount() {
        List<Review> allReviews = reviewDAO.getByUserId(currentUserId);
        Set<Integer> uniqueQuestionIds = new HashSet<>();
        for (Review review : allReviews) {
            uniqueQuestionIds.add(review.getQuestionId());
        }
        return uniqueQuestionIds.size();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setStyle(
                "-fx-background-color: #02A1D4;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 5, 0);"
        );

        // Logo/Brand
        VBox brandBox = new VBox(5);
        brandBox.setAlignment(Pos.CENTER);
        brandBox.setPadding(new Insets(0, 0, 20, 0));

        Label brandIcon = new Label("🎓");
        brandIcon.setFont(Font.font(40));

        Label brandName = new Label("Smart Study");
        brandName.setFont(Font.font("System", FontWeight.BOLD, 20));
        brandName.setStyle("-fx-text-fill: white;");

        brandBox.getChildren().addAll(brandIcon, brandName);

        // Storage info card
        VBox storageCard = createStorageCard();

        // Navigation menu
        VBox navMenu = new VBox(6);
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
        activeNavButton = homeBtn;

        // Bottom spacer
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        // Version info
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");
        versionLabel.setAlignment(Pos.CENTER);
        versionLabel.setMaxWidth(Double.MAX_VALUE);

        sidebar.getChildren().addAll(brandBox, storageCard, navMenu, bottomSpacer, versionLabel);

        return sidebar;
    }

    private VBox createStorageCard() {
        VBox storageCard = new VBox(12);
        storageCard.setPadding(new Insets(20));
        storageCard.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 12, 0, 0, 4);"
        );

        Label storageLabel = new Label("Study Progress");
        storageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 13px;");

        int totalQuestions = questionDAO.getCount();
        int reviewedQuestions = getUniqueReviewedCount();

        Label storageText = new Label(reviewedQuestions + " / " + totalQuestions);
        storageText.setFont(Font.font("System", FontWeight.BOLD, 24));
        storageText.setStyle("-fx-text-fill: white;");

        Label storageSubtext = new Label("Questions Reviewed");
        storageSubtext.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(totalQuestions > 0 ? (double) reviewedQuestions / totalQuestions : 0);
        progressBar.setPrefWidth(220);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
                "-fx-accent: white; " +
                        "-fx-background-color: rgba(255,255,255,0.2); " +
                        "-fx-background-radius: 3;"
        );

        storageCard.getChildren().addAll(storageLabel, storageText, storageSubtext, progressBar);

        return storageCard;
    }

    private void refreshSidebar() {
        // Update the storage card with fresh data
        VBox newStorageCard = createStorageCard();

        // Find and replace the old storage card (it's the second child after brandBox)
        if (sidebar.getChildren().size() > 1) {
            sidebar.getChildren().set(1, newStorageCard);
        }
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(240);
        btn.setPrefHeight(44);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("System", FontWeight.NORMAL, 14));

        String baseStyle =
                "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0 0 0 20;";

        if (active) {
            btn.setStyle(baseStyle +
                    "-fx-background-color: rgba(255,255,255,0.15); " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold;"
            );
        } else {
            btn.setStyle(baseStyle +
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: rgba(255,255,255,0.7);"
            );
        }

        // Hover effects
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("font-weight: bold")) {
                btn.setStyle(baseStyle +
                        "-fx-background-color: rgba(255,255,255,0.1); " +
                        "-fx-text-fill: white;"
                );
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("font-weight: bold")) {
                btn.setStyle(baseStyle +
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: rgba(255,255,255,0.7);"
                );
            }
        });

        return btn;
    }

    private void showDashboard() {
        VBox dashboard = new VBox(30);
        dashboard.setPadding(new Insets(40, 50, 40, 50));
        dashboard.setStyle("-fx-background-color: #f8fafc;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(8);
        Label title = new Label("Dashboard");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: #0f172a;");

        com.semisaad.smartstudy.model.User user = userDAO.getById(currentUserId);
        String username = user != null ? user.getUsername() : "User";

        Label welcome = new Label("👋 Welcome back, " + username + "!");
        welcome.setFont(Font.font(16));
        welcome.setStyle("-fx-text-fill: #64748b;");
        headerText.getChildren().addAll(title, welcome);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quick start study button
        Button quickStudyBtn = new Button("🎯 Start Study Session");
        quickStudyBtn.setPrefHeight(48);
        quickStudyBtn.setPrefWidth(200);
        quickStudyBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        quickStudyBtn.setStyle(
                "-fx-background-color: #34aeeb; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        quickStudyBtn.setOnAction(e -> showStudySession());

        header.getChildren().addAll(headerText, spacer, quickStudyBtn);

        // Stats cards
        var stats = studyService.getSessionStats(currentUserId);
        int dueToday = stats.getQuestionsDueToday();

        HBox statsRow = new HBox(20);
        VBox dueCard = createStatCard("⏰", "Due Today", String.valueOf(dueToday),
                dueToday > 0 ? "Start studying!" : "All caught up!", "#3b82f6");
        VBox successCard = createStatCard("✨", "Success Rate", String.format("%.0f%%", stats.getSuccessRate()),
                "Keep improving!", "#10b981");
        VBox totalCard = createStatCard("🔥", "Total Reviews", String.valueOf(stats.getTotalReviews()),
                "Questions studied", "#f59e0b");
        VBox streakCard = createStatCard("📅", "Study Streak", calculateStreak() + " days",
                "Daily goal: " + dailyGoalQuestions + " questions", "#8b5cf6");

        statsRow.getChildren().addAll(dueCard, successCard, totalCard, streakCard);

        // Topics section
        HBox topicsHeader = new HBox();
        topicsHeader.setAlignment(Pos.CENTER_LEFT);
        Label topicsTitle = new Label("Your Topics");
        topicsTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        topicsTitle.setStyle("-fx-text-fill: #0f172a;");

        Region topicSpacer = new Region();
        HBox.setHgrow(topicSpacer, Priority.ALWAYS);

        Button viewAllTopics = new Button("View All →");
        viewAllTopics.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14px;"
        );
        viewAllTopics.setOnAction(e -> showTopics());

        topicsHeader.getChildren().addAll(topicsTitle, topicSpacer, viewAllTopics);

        // Topics grid
        FlowPane topicsGrid = new FlowPane(20, 20);

        List<Topic> topics = topicDAO.getAll();
        String[] colors = {"#3b82f6", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#06b6d4"};
        String[] icons = {"📦", "🔄", "🎯", "🗄️", "💻", "🌐"};

        if (topics.isEmpty()) {
            VBox emptyTopics = createEmptyState(
                    "📁",
                    "No topics yet",
                    "Create your first topic to start organizing",
                    "Create Topic",
                    () -> {
                        showTopics();
                        showAddTopicDialog();
                    }
            );
            topicsGrid.getChildren().add(emptyTopics);
        } else {
            for (int i = 0; i < Math.min(topics.size(), 4); i++) {
                Topic topic = topics.get(i);
                int questionCount = questionDAO.getByTopicId(topic.getId()).size();
                VBox topicCard = createTopicCard(topic.getName(), questionCount + " questions",
                        icons[i % icons.length], colors[i % colors.length]);
                topicsGrid.getChildren().add(topicCard);
            }
        }

        // Recent activity
        HBox questionsHeader = new HBox();
        questionsHeader.setAlignment(Pos.CENTER_LEFT);
        Label questionsTitle = new Label("Recent Activity");
        questionsTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        questionsTitle.setStyle("-fx-text-fill: #0f172a;");
        questionsHeader.getChildren().add(questionsTitle);

        VBox questionsTable = createSimpleQuestionsTable();

        dashboard.getChildren().addAll(header, statsRow, topicsHeader, topicsGrid, questionsHeader, questionsTable);

        ScrollPane scrollPane = new ScrollPane(dashboard);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createStatCard(String icon, String label, String value, String change, String accentColor) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setPrefWidth(260);
        card.setPrefHeight(140);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(28));
        iconLabel.setPrefSize(56, 56);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle(
                "-fx-background-color: " + accentColor + "1A; " +
                        "-fx-background-radius: 14;"
        );

        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.NORMAL, 13));
        labelText.setStyle("-fx-text-fill: #64748b;");

        Label valueText = new Label(value);
        valueText.setFont(Font.font("System", FontWeight.BOLD, 36));
        valueText.setStyle("-fx-text-fill: #0f172a;");

        Label changeText = new Label(change);
        changeText.setFont(Font.font(12));
        changeText.setStyle("-fx-text-fill: " + accentColor + ";");

        card.getChildren().addAll(iconLabel, labelText, valueText, changeText);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8); " +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
            );
        });

        return card;
    }

    private VBox createTopicCard(String name, String count, String icon, String color) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setPrefWidth(240);
        card.setPrefHeight(160);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(60, 60);
        iconContainer.setStyle(
                "-fx-background-color: linear-gradient(135deg, " + color + " 0%, " + color + "CC 100%); " +
                        "-fx-background-radius: 16;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(30));
        iconContainer.getChildren().add(iconLabel);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #0f172a;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190);

        Label countLabel = new Label(count);
        countLabel.setFont(Font.font(13));
        countLabel.setStyle("-fx-text-fill: #64748b;");

        card.getChildren().addAll(iconContainer, nameLabel, countLabel);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, " + color + "40, 16, 0, 0, 8);"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
            );
        });

        return card;
    }

    private VBox createSimpleQuestionsTable() {
        VBox table = new VBox();
        table.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );
        table.setMaxHeight(350);

        HBox header = new HBox();
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setSpacing(20);
        header.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 20 20 0 0;");

        Label iconHeader = new Label("");
        iconHeader.setPrefWidth(40);

        Label nameHeader = new Label("QUESTION");
        nameHeader.setPrefWidth(400);
        nameHeader.setFont(Font.font("System", FontWeight.BOLD, 11));
        nameHeader.setStyle("-fx-text-fill: #64748b;");

        Label topicHeader = new Label("TOPIC");
        topicHeader.setPrefWidth(200);
        topicHeader.setFont(Font.font("System", FontWeight.BOLD, 11));
        topicHeader.setStyle("-fx-text-fill: #64748b;");

        Label statusHeader = new Label("STATUS");
        statusHeader.setPrefWidth(150);
        statusHeader.setFont(Font.font("System", FontWeight.BOLD, 11));
        statusHeader.setStyle("-fx-text-fill: #64748b;");

        header.getChildren().addAll(iconHeader, nameHeader, topicHeader, statusHeader);

        VBox rows = new VBox(0);

        List<Question> questions = questionDAO.getAll();
        for (int i = 0; i < Math.min(questions.size(), 5); i++) {
            Question q = questions.get(i);
            Topic topic = topicDAO.getById(q.getTopicId());

            Review latestReview = reviewDAO.getLatestReview(q.getId(), currentUserId);
            String status = latestReview == null ? "New" :
                    (latestReview.getNextReviewDate().isBefore(LocalDate.now()) ? "Due" : "Reviewed");
            String statusColor = latestReview == null ? "#3b82f6" :
                    (latestReview.getNextReviewDate().isBefore(LocalDate.now()) ? "#f59e0b" : "#10b981");

            HBox row = new HBox();
            row.setPadding(new Insets(16, 24, 16, 24));
            row.setSpacing(20);
            row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 1 0 0 0;");

            Label icon = new Label("📝");
            icon.setFont(Font.font(20));
            icon.setPrefWidth(40);

            String questionText = q.getQuestionText();
            if (questionText.length() > 50) {
                questionText = questionText.substring(0, 50) + "...";
            }
            Label name = new Label(questionText);
            name.setPrefWidth(400);
            name.setFont(Font.font(14));
            name.setStyle("-fx-text-fill: #0f172a;");

            Label topicName = new Label(topic != null ? topic.getName() : "Unknown");
            topicName.setPrefWidth(200);
            topicName.setFont(Font.font(13));
            topicName.setStyle("-fx-text-fill: #64748b;");

            Label statusLabel = new Label(status);
            statusLabel.setPrefWidth(150);
            statusLabel.setPadding(new Insets(4, 12, 4, 12));
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            statusLabel.setStyle(
                    "-fx-background-color: " + statusColor + "1A; " +
                            "-fx-text-fill: " + statusColor + "; " +
                            "-fx-background-radius: 12;"
            );

            row.getChildren().addAll(icon, name, topicName, statusLabel);

            row.setOnMouseEntered(e -> row.setStyle(
                    "-fx-background-color: #f8fafc; " +
                            "-fx-border-color: #f1f5f9; " +
                            "-fx-border-width: 1 0 0 0;"
            ));
            row.setOnMouseExited(e -> row.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-border-color: #f1f5f9; " +
                            "-fx-border-width: 1 0 0 0;"
            ));

            rows.getChildren().add(row);
        }

        table.getChildren().addAll(header, rows);

        return table;
    }

    private void showTopics() {
        VBox topicsScreen = new VBox(25);
        topicsScreen.setPadding(new Insets(40, 50, 40, 50));
        topicsScreen.setStyle("-fx-background-color: #f8fafc;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label title = new Label("Topics");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addTopicBtn = new Button("＋ Add Topic");
        addTopicBtn.setPrefHeight(44);
        addTopicBtn.setPrefWidth(140);
        addTopicBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        addTopicBtn.setStyle(
                "-fx-background-color: #34aeeb;" +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        addTopicBtn.setOnAction(e -> showAddTopicDialog());

        header.getChildren().addAll(title, spacer, addTopicBtn);

        FlowPane topicsGrid = new FlowPane(24, 24);

        List<Topic> topics = topicDAO.getAll();
        String[] colors = {"#3b82f6", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#06b6d4", "#f97316", "#ef4444"};
        String[] icons = {"📦", "🔄", "🎯", "🗄️", "💻", "🌐", "📚", "🧠"};

        if (topics.isEmpty()) {
            VBox emptyState = createEmptyState(
                    "📁",
                    "No topics yet",
                    "Create your first topic to start organizing your questions",
                    "Create Topic",
                    () -> showAddTopicDialog()
            );
            topicsGrid.getChildren().add(emptyState);
        } else {
            for (int i = 0; i < topics.size(); i++) {
                Topic topic = topics.get(i);
                int questionCount = questionDAO.getByTopicId(topic.getId()).size();
                VBox topicCard = createManageableTopicCard(
                        topic,
                        questionCount,
                        icons[i % icons.length],
                        colors[i % colors.length]
                );
                topicsGrid.getChildren().add(topicCard);
            }
        }

        topicsScreen.getChildren().addAll(header, topicsGrid);

        ScrollPane scrollPane = new ScrollPane(topicsScreen);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createEmptyState(String icon, String title, String message, String buttonText, Runnable action) {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(80));
        emptyState.setPrefWidth(600);
        emptyState.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 24; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(64));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #0f172a;");

        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font(14));
        messageLabel.setStyle("-fx-text-fill: #64748b;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setAlignment(Pos.CENTER);

        Button actionBtn = new Button(buttonText);
        actionBtn.setPrefHeight(44);
        actionBtn.setPrefWidth(160);
        actionBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        actionBtn.setStyle(
                "-fx-background-color: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        actionBtn.setOnAction(e -> action.run());

        emptyState.getChildren().addAll(iconLabel, titleLabel, messageLabel, actionBtn);

        return emptyState;
    }

    private VBox createManageableTopicCard(Topic topic, int questionCount, String icon, String color) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setPrefWidth(280);
        card.setPrefHeight(220);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(64, 64);
        iconContainer.setStyle(
                "-fx-background-color: linear-gradient(135deg, " + color + " 0%, " + color + "CC 100%); " +
                        "-fx-background-radius: 18;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(32));
        iconContainer.getChildren().add(iconLabel);

        Label nameLabel = new Label(topic.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 17));
        nameLabel.setStyle("-fx-text-fill: #0f172a;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(220);

        HBox countBox = new HBox(8);
        countBox.setAlignment(Pos.CENTER_LEFT);

        Label countBadge = new Label(String.valueOf(questionCount));
        countBadge.setPadding(new Insets(4, 10, 4, 10));
        countBadge.setFont(Font.font("System", FontWeight.BOLD, 12));
        countBadge.setStyle(
                "-fx-background-color: " + color + "1A; " +
                        "-fx-text-fill: " + color + "; " +
                        "-fx-background-radius: 10;"
        );

        Label countLabel = new Label("questions");
        countLabel.setFont(Font.font(13));
        countLabel.setStyle("-fx-text-fill: #64748b;");

        countBox.getChildren().addAll(countBadge, countLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Edit");
        editBtn.setPrefWidth(110);
        editBtn.setPrefHeight(36);
        editBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        editBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
        editBtn.setOnAction(e -> {
            e.consume();
            showEditTopicDialog(topic);
        });

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setPrefHeight(36);
        deleteBtn.setPrefWidth(36);
        deleteBtn.setStyle(
                "-fx-background-color: #fef2f2; " +
                        "-fx-text-fill: #ef4444; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> {
            e.consume();
            deleteTopic(topic, questionCount);
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(iconContainer, nameLabel, countBox, spacer, actions);

        card.setOnMouseEntered(e -> {
            if (e.getTarget() == card || !e.getTarget().toString().contains("Button")) {
                card.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-background-radius: 20; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(gaussian, " + color + "30, 20, 0, 0, 8);"
                );
            }
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 20; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
            );
        });

        return card;
    }

    private void showAddTopicDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Topic");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogContent = new VBox(24);
        dialogContent.setPadding(new Insets(40));
        dialogContent.setPrefWidth(500);
        dialogContent.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Label title = new Label("Create New Topic");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #0f172a;");

        VBox nameBox = new VBox(10);
        Label nameLabel = new Label("Topic Name");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #0f172a;");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Data Structures, Algorithms...");
        nameField.setPrefHeight(48);
        nameField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        nameBox.getChildren().addAll(nameLabel, nameField);

        VBox descBox = new VBox(10);
        Label descLabel = new Label("Description (optional)");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        descLabel.setStyle("-fx-text-fill: #0f172a;");

        TextArea descField = new TextArea();
        descField.setPromptText("Brief description of this topic...");
        descField.setPrefRowCount(4);
        descField.setWrapText(true);
        descField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        descBox.getChildren().addAll(descLabel, descField);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(44);
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        cancelBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Create Topic");
        saveBtn.setPrefWidth(140);
        saveBtn.setPrefHeight(44);
        saveBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        saveBtn.setStyle(
                "-fx-background-color: #34aeeb; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String description = descField.getText().trim();

            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Topic Name Required",
                        "Please enter a topic name.");
                return;
            }

            Topic newTopic = new Topic(name, description);
            boolean saved = topicDAO.insert(newTopic);

            if (saved) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Topic Created!",
                        "The topic '" + name + "' has been created successfully.");
                dialog.close();
                showTopics();
                refreshSidebar();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Save Failed",
                        "Could not create the topic. It might already exist.");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(title, nameBox, descBox, buttonBox);

        Scene dialogScene = new Scene(dialogContent);
        dialog.setScene(dialogScene);
        dialog.setMinHeight(600);
        dialog.setMinWidth(750);
        dialog.show();
    }

    private void showEditTopicDialog(Topic topic) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Topic");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogContent = new VBox(24);
        dialogContent.setPadding(new Insets(40));
        dialogContent.setPrefWidth(500);
        dialogContent.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Label title = new Label("Edit Topic");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #0f172a;");

        VBox nameBox = new VBox(10);
        Label nameLabel = new Label("Topic Name");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #0f172a;");

        TextField nameField = new TextField(topic.getName());
        nameField.setPrefHeight(48);
        nameField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        nameBox.getChildren().addAll(nameLabel, nameField);

        VBox descBox = new VBox(10);
        Label descLabel = new Label("Description");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        descLabel.setStyle("-fx-text-fill: #0f172a;");

        TextArea descField = new TextArea(topic.getDescription());
        descField.setPrefRowCount(4);
        descField.setWrapText(true);
        descField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        descBox.getChildren().addAll(descLabel, descField);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(44);
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        cancelBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setPrefWidth(140);
        saveBtn.setPrefHeight(44);
        saveBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        saveBtn.setStyle(
                "-fx-background-color: #34aeeb; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        saveBtn.setOnAction(e -> {
            topic.setName(nameField.getText().trim());
            topic.setDescription(descField.getText().trim());

            boolean updated = topicDAO.update(topic);

            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Topic Updated!",
                        "The topic has been updated successfully.");
                dialog.close();
                showTopics();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(title, nameBox, descBox, buttonBox);

        Scene dialogScene = new Scene(dialogContent);
        dialog.setScene(dialogScene);
        dialog.setMinHeight(650);
        dialog.setMinWidth(700);
        dialog.show();
    }

    private void deleteTopic(Topic topic, int questionCount) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Topic");
        confirmation.setHeaderText("Are you sure?");

        if (questionCount > 0) {
            confirmation.setContentText(
                    "This topic has " + questionCount + " question(s).\n" +
                            "Deleting it will also delete all associated questions!\n\n" +
                            "Topic: " + topic.getName()
            );
        } else {
            confirmation.setContentText("Delete topic: " + topic.getName() + "?");
        }

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = topicDAO.delete(topic.getId());

                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Topic Deleted",
                            "The topic has been deleted successfully.");
                    showTopics();
                    refreshSidebar();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed",
                            "Could not delete the topic. Please try again.");
                }
            }
        });
    }

    private void showQuestions() {
        VBox questionsScreen = new VBox(25);
        questionsScreen.setPadding(new Insets(40, 50, 40, 50));
        questionsScreen.setStyle("-fx-background-color: #f8fafc;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label title = new Label("Questions");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addQuestionBtn = new Button("＋ Add Question");
        addQuestionBtn.setPrefHeight(44);
        addQuestionBtn.setPrefWidth(150);
        addQuestionBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        addQuestionBtn.setStyle(
                "-fx-background-color: #34aeeb; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        addQuestionBtn.setOnAction(e -> showAddQuestionDialog());

        header.getChildren().addAll(title, spacer, addQuestionBtn);

        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search questions...");
        searchField.setPrefWidth(350);
        searchField.setPrefHeight(44);
        searchField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-padding: 12; " +
                        "-fx-font-size: 14px;"
        );

        ComboBox<String> topicFilter = new ComboBox<>();
        topicFilter.getItems().add("All Topics");
        List<Topic> topics = topicDAO.getAll();
        for (Topic topic : topics) {
            topicFilter.getItems().add(topic.getName());
        }
        topicFilter.setValue("All Topics");
        topicFilter.setPrefHeight(44);
        topicFilter.setPrefWidth(180);
        topicFilter.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12;"
        );

        searchBox.getChildren().addAll(searchField, topicFilter);

        VBox tableContainer = new VBox();
        tableContainer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(20, 24, 16, 24));
        tableHeader.setStyle("-fx-background-color: #f8fafc; -fx-border-radius: 20 20 0 0; -fx-background-radius: 20 20 0 0;");
        tableHeader.setSpacing(20);

        Label headerQuestion = new Label("QUESTION");
        headerQuestion.setPrefWidth(350);
        headerQuestion.setFont(Font.font("System", FontWeight.BOLD, 11));
        headerQuestion.setStyle("-fx-text-fill: #64748b;");

        Label headerTopic = new Label("TOPIC");
        headerTopic.setPrefWidth(150);
        headerTopic.setFont(Font.font("System", FontWeight.BOLD, 11));
        headerTopic.setStyle("-fx-text-fill: #64748b;");

        Label headerDifficulty = new Label("DIFFICULTY");
        headerDifficulty.setPrefWidth(120);
        headerDifficulty.setFont(Font.font("System", FontWeight.BOLD, 11));
        headerDifficulty.setStyle("-fx-text-fill: #64748b;");

        Label headerActions = new Label("ACTIONS");
        headerActions.setPrefWidth(150);
        headerActions.setFont(Font.font("System", FontWeight.BOLD, 11));
        headerActions.setStyle("-fx-text-fill: #64748b;");

        tableHeader.getChildren().addAll(headerQuestion, headerTopic, headerDifficulty, headerActions);

        VBox tableRows = new VBox(0);

        List<Question> questions = questionDAO.getAll();

        for (Question q : questions) {
            Topic topic = topicDAO.getById(q.getTopicId());
            HBox row = createQuestionRow(q, topic);
            tableRows.getChildren().add(row);
        }

        ScrollPane tableScroll = new ScrollPane(tableRows);
        tableScroll.setFitToWidth(true);
        tableScroll.setPrefHeight(450);
        tableScroll.setStyle("-fx-background-color: white; -fx-background: white;");

        tableContainer.getChildren().addAll(tableHeader, tableScroll);

        searchField.textProperty().addListener((obs, old, newVal) -> {
            updateQuestionsList(tableRows, newVal, topicFilter.getValue());
        });

        topicFilter.setOnAction(e -> {
            updateQuestionsList(tableRows, searchField.getText(), topicFilter.getValue());
        });

        questionsScreen.getChildren().addAll(header, searchBox, tableContainer);

        ScrollPane mainScroll = new ScrollPane(questionsScreen);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(mainScroll);
    }

    private HBox createQuestionRow(Question q, Topic topic) {
        HBox row = new HBox();
        row.setPadding(new Insets(16, 24, 16, 24));
        row.setSpacing(20);
        row.setStyle(
                "-fx-border-color: #f1f5f9; " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-color: white;"
        );

        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-border-color: #f1f5f9; " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-color: #f8fafc;"
        ));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-border-color: #f1f5f9; " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-color: white;"
        ));

        Label questionLabel = new Label(q.getQuestionText());
        questionLabel.setPrefWidth(350);
        questionLabel.setWrapText(true);
        questionLabel.setMaxHeight(60);
        questionLabel.setFont(Font.font(14));
        questionLabel.setStyle("-fx-text-fill: #0f172a;");

        Label topicLabel = new Label(topic != null ? topic.getName() : "Unknown");
        topicLabel.setPrefWidth(150);
        topicLabel.setFont(Font.font(13));
        topicLabel.setStyle("-fx-text-fill: #64748b;");

        Label difficultyLabel = new Label(q.getDifficulty());
        difficultyLabel.setPrefWidth(120);
        difficultyLabel.setPadding(new Insets(6, 12, 6, 12));
        difficultyLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        difficultyLabel.setStyle(
                "-fx-background-radius: 12; " +
                        getDifficultyColor(q.getDifficulty())
        );

        HBox actions = new HBox(8);
        actions.setPrefWidth(150);

        Button editBtn = new Button("Edit");
        editBtn.setPrefHeight(32);
        editBtn.setPrefWidth(60);
        editBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        editBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        editBtn.setOnAction(e -> showEditQuestionDialog(q));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setPrefHeight(32);
        deleteBtn.setPrefWidth(32);
        deleteBtn.setStyle(
                "-fx-background-color: #fef2f2; " +
                        "-fx-text-fill: #ef4444; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> deleteQuestion(q));

        actions.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(questionLabel, topicLabel, difficultyLabel, actions);

        return row;
    }

    private void updateQuestionsList(VBox tableRows, String searchText, String topicFilter) {
        tableRows.getChildren().clear();

        List<Question> questions = questionDAO.getAll();

        for (Question q : questions) {
            Topic topic = topicDAO.getById(q.getTopicId());

            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    q.getQuestionText().toLowerCase().contains(searchText.toLowerCase()) ||
                    q.getAnswer().toLowerCase().contains(searchText.toLowerCase());

            boolean matchesTopic = topicFilter.equals("All Topics") ||
                    (topic != null && topic.getName().equals(topicFilter));

            if (matchesSearch && matchesTopic) {
                HBox row = createQuestionRow(q, topic);
                tableRows.getChildren().add(row);
            }
        }
    }

    private void showAddQuestionDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Question");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setOnCloseRequest(e -> showQuestions());

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(40));
        dialogContent.setPrefWidth(600);
        dialogContent.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Label title = new Label("Add New Question");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #0f172a;");

        VBox questionBox = new VBox(10);
        Label questionLabel = new Label("Question");
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        questionLabel.setStyle("-fx-text-fill: #0f172a;");

        TextArea questionField = new TextArea();
        questionField.setPromptText("Enter your question here...");
        questionField.setPrefRowCount(3);
        questionField.setWrapText(true);
        questionField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        questionBox.getChildren().addAll(questionLabel, questionField);

        VBox answerBox = new VBox(10);
        Label answerLabel = new Label("Answer");
        answerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        answerLabel.setStyle("-fx-text-fill: #0f172a;");

        TextArea answerField = new TextArea();
        answerField.setPromptText("Enter the correct answer...");
        answerField.setPrefRowCount(3);
        answerField.setWrapText(true);
        answerField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        answerBox.getChildren().addAll(answerLabel, answerField);

        HBox metaRow = new HBox(15);

        VBox topicBox = new VBox(10);
        HBox.setHgrow(topicBox, Priority.ALWAYS);
        Label topicLabel = new Label("Topic");
        topicLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        topicLabel.setStyle("-fx-text-fill: #0f172a;");

        ComboBox<String> topicCombo = new ComboBox<>();
        List<Topic> topics = topicDAO.getAll();
        for (Topic t : topics) {
            topicCombo.getItems().add(t.getName());
        }
        if (!topics.isEmpty()) {
            topicCombo.setValue(topics.get(0).getName());
        }
        topicCombo.setPrefHeight(48);
        topicCombo.setMaxWidth(Double.MAX_VALUE);
        topicCombo.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12;"
        );
        topicBox.getChildren().addAll(topicLabel, topicCombo);

        VBox difficultyBox = new VBox(10);
        HBox.setHgrow(difficultyBox, Priority.ALWAYS);
        Label difficultyLabel = new Label("Difficulty");
        difficultyLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        difficultyLabel.setStyle("-fx-text-fill: #0f172a;");

        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("EASY", "MEDIUM", "HARD");
        difficultyCombo.setValue("MEDIUM");
        difficultyCombo.setPrefHeight(48);
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);
        difficultyCombo.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12;"
        );
        difficultyBox.getChildren().addAll(difficultyLabel, difficultyCombo);

        metaRow.getChildren().addAll(topicBox, difficultyBox);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(44);
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        cancelBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand;"
        );

        cancelBtn.setOnAction(e -> {
            dialog.close();
            showQuestions();  // refresh list when user manually closes
        });

        Button saveBtn = new Button("Save Question");
        saveBtn.setPrefWidth(150);
        saveBtn.setPrefHeight(44);
        saveBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        saveBtn.setStyle(
                "-fx-background-color: #34aeeb; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );

        Label successMsg = new Label();
        successMsg.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13px;");

        saveBtn.setOnAction(e -> {
            String questionText = questionField.getText().trim();
            String answer = answerField.getText().trim();
            String topicName = topicCombo.getValue();
            String difficulty = difficultyCombo.getValue();

            if (questionText.isEmpty() || answer.isEmpty()) {
                successMsg.setText(""); // clear success if they retry
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Missing Information",
                        "Please fill in both question and answer fields.");
                return;
            }

            int topicId = 0;
            for (Topic t : topics) {
                if (t.getName().equals(topicName)) {
                    topicId = t.getId();
                    break;
                }
            }

            Question newQuestion = new Question(questionText, answer, topicId, difficulty);
            boolean saved = questionDAO.insert(newQuestion);

            if (saved) {
                questionField.clear();
                answerField.clear();
                successMsg.setText("✅ Question saved! Add another or close when done.");
                refreshSidebar();
            } else {
                successMsg.setText("");
                showAlert(Alert.AlertType.ERROR, "Error", "Save Failed",
                        "Could not save the question. Please try again.");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(
                title,
                questionBox,
                answerBox,
                metaRow,
                buttonBox,
                successMsg
        );

        Scene dialogScene = new Scene(dialogContent);
        dialog.setMinHeight(600);
        dialog.setMinWidth(750);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showEditQuestionDialog(Question question) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Question");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(40));
        dialogContent.setPrefWidth(600);
        dialogContent.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Label title = new Label("Edit Question");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #0f172a;");

        VBox questionBox = new VBox(10);
        Label questionLabel = new Label("Question");
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        questionLabel.setStyle("-fx-text-fill: #0f172a;");

        TextArea questionField = new TextArea(question.getQuestionText());
        questionField.setPrefRowCount(3);
        questionField.setWrapText(true);
        questionField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        questionBox.getChildren().addAll(questionLabel, questionField);

        VBox answerBox = new VBox(10);
        Label answerLabel = new Label("Answer");
        answerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        answerLabel.setStyle("-fx-text-fill: #0f172a;");

        TextArea answerField = new TextArea(question.getAnswer());
        answerField.setPrefRowCount(3);
        answerField.setWrapText(true);
        answerField.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12;"
        );
        answerBox.getChildren().addAll(answerLabel, answerField);

        HBox metaRow = new HBox(15);

        VBox topicBox = new VBox(10);
        HBox.setHgrow(topicBox, Priority.ALWAYS);
        Label topicLabel = new Label("Topic");
        topicLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        topicLabel.setStyle("-fx-text-fill: #0f172a;");

        ComboBox<String> topicCombo = new ComboBox<>();
        List<Topic> topics = topicDAO.getAll();
        Topic currentTopic = topicDAO.getById(question.getTopicId());
        for (Topic t : topics) {
            topicCombo.getItems().add(t.getName());
        }
        topicCombo.setValue(currentTopic != null ? currentTopic.getName() : topics.get(0).getName());
        topicCombo.setPrefHeight(48);
        topicCombo.setMaxWidth(Double.MAX_VALUE);
        topicCombo.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12;"
        );
        topicBox.getChildren().addAll(topicLabel, topicCombo);

        VBox difficultyBox = new VBox(10);
        HBox.setHgrow(difficultyBox, Priority.ALWAYS);
        Label difficultyLabel = new Label("Difficulty");
        difficultyLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        difficultyLabel.setStyle("-fx-text-fill: #0f172a;");

        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("EASY", "MEDIUM", "HARD");
        difficultyCombo.setValue(question.getDifficulty());
        difficultyCombo.setPrefHeight(48);
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);
        difficultyCombo.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12;"
        );
        difficultyBox.getChildren().addAll(difficultyLabel, difficultyCombo);

        metaRow.getChildren().addAll(topicBox, difficultyBox);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(44);
        cancelBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        cancelBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setPrefWidth(150);
        saveBtn.setPrefHeight(44);
        saveBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        saveBtn.setStyle(
                "-fx-background-color: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        saveBtn.setOnAction(e -> {
            question.setQuestionText(questionField.getText().trim());
            question.setAnswer(answerField.getText().trim());
            question.setDifficulty(difficultyCombo.getValue());

            for (Topic t : topics) {
                if (t.getName().equals(topicCombo.getValue())) {
                    question.setTopicId(t.getId());
                    break;
                }
            }

            boolean updated = questionDAO.update(question);

            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Question Updated!",
                        "The question has been updated successfully.");
                dialog.close();
                showQuestions();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(
                title,
                questionBox,
                answerBox,
                metaRow,
                buttonBox
        );

        Scene dialogScene = new Scene(dialogContent);
        dialog.setScene(dialogScene);
        dialog.setMinHeight(600);
        dialog.setMinWidth(750);
        dialog.show();
    }

    private void deleteQuestion(Question question) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Question");
        confirmation.setHeaderText("Are you sure?");
        confirmation.setContentText("Do you want to delete this question?\n\n\"" +
                question.getQuestionText().substring(0, Math.min(50, question.getQuestionText().length())) + "...\"");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = questionDAO.delete(question.getId());

                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Question Deleted",
                            "The question has been deleted successfully.");
                    showQuestions();
                    refreshSidebar();
                }
            }
        });
    }

    private void showStudySession() {
        // Only reset if this is a fresh session start, not mid-session navigation
        answeredThisSession.clear();

        List<Question> dueQuestions = studyService.getDueQuestions(currentUserId);

        // Remove any already answered + cap to daily goal
        dueQuestions.removeIf(q -> answeredThisSession.contains(q.getId()));
        if (dueQuestions.size() > dailyGoalQuestions) {
            dueQuestions = new ArrayList<>(dueQuestions.subList(0, dailyGoalQuestions));
        }

        currentStudyQuestions = dueQuestions;

        // If no due questions, fill remaining slots with new questions
        if (currentStudyQuestions.isEmpty()) {
            currentStudyQuestions = studyService.getNewQuestions(
                    currentUserId,
                    dailyGoalQuestions,
                    new ArrayList<>(answeredThisSession)  // pass excluded IDs
            );
        } else if (currentStudyQuestions.size() < dailyGoalQuestions) {
            // Optionally: top up with new questions if due < daily goal
            int remaining = dailyGoalQuestions - currentStudyQuestions.size();
            List<Question> topUp = studyService.getNewQuestions(
                    currentUserId,
                    remaining,
                    currentStudyQuestions.stream()
                            .map(Question::getId)
                            .collect(java.util.stream.Collectors.toList())
            );
            currentStudyQuestions.addAll(topUp);
        }

        currentQuestionIndex = 0;

        if (currentStudyQuestions.isEmpty()) {
            showNoQuestionsScreen();
            return;
        }

        showQuestionCard();
    }

    private void showNoQuestionsScreen() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(100));
        content.setStyle("-fx-background-color: #f8fafc;");

        Label icon = new Label("🎉");
        icon.setFont(Font.font(80));

        Label title = new Label("All Caught Up!");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: #0f172a;");

        Label message = new Label("You have no questions due for review today.");
        message.setFont(Font.font(16));
        message.setStyle("-fx-text-fill: #64748b;");

        var stats = studyService.getSessionStats(currentUserId);
        Label statsInfo = new Label(String.format("You've reviewed %d questions with a %.0f%% success rate!",
                stats.getTotalReviews(), stats.getSuccessRate()));
        statsInfo.setFont(Font.font(14));
        statsInfo.setStyle("-fx-text-fill: #10b981;");

        Button backBtn = new Button("⬅️ Back to Dashboard");
        backBtn.setPrefHeight(44);
        backBtn.setPrefWidth(200);
        backBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        backBtn.setStyle(
                "-fx-background-color: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        backBtn.setOnAction(e -> showDashboard());

        content.getChildren().addAll(icon, title, message, statsInfo, backBtn);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    private void showQuestionCard() {
        Question currentQuestion = currentStudyQuestions.get(currentQuestionIndex);
        Topic topic = topicDAO.getById(currentQuestion.getTopicId());

        VBox questionScreen = new VBox(30);
        questionScreen.setPadding(new Insets(50));
        questionScreen.setAlignment(Pos.TOP_CENTER);
        questionScreen.setStyle("-fx-background-color: #f8fafc;");

        HBox progressBar = new HBox(10);
        progressBar.setAlignment(Pos.CENTER);

        ProgressBar progressIndicator = new ProgressBar();
        progressIndicator.setProgress((double) (currentQuestionIndex + 1) / currentStudyQuestions.size());
        progressIndicator.setPrefWidth(300);
        progressIndicator.setPrefHeight(8);
        progressIndicator.setStyle(
                "-fx-accent: linear-gradient(to right, #667eea, #764ba2);" +
                        "-fx-background-color: #e2e8f0;" +
                        "-fx-background-radius: 4;"
        );

        Label progressText = new Label("Question " + (currentQuestionIndex + 1) + " of " + currentStudyQuestions.size());
        progressText.setFont(Font.font("System", FontWeight.BOLD, 14));
        progressText.setStyle("-fx-text-fill: #64748b;");

        VBox progressBox = new VBox(10);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.getChildren().addAll(progressIndicator, progressText);

        VBox questionCard = new VBox(25);
        questionCard.setPadding(new Insets(50));
        questionCard.setMaxWidth(800);
        questionCard.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 24; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 8);"
        );

        HBox cardHeader = new HBox(15);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label topicLabel = new Label("📁 " + (topic != null ? topic.getName() : "Unknown"));
        topicLabel.setFont(Font.font(14));
        topicLabel.setStyle("-fx-text-fill: #64748b;");

        Label difficultyBadge = new Label(currentQuestion.getDifficulty());
        difficultyBadge.setPadding(new Insets(6, 14, 6, 14));
        difficultyBadge.setFont(Font.font("System", FontWeight.BOLD, 12));
        difficultyBadge.setStyle(
                "-fx-background-radius: 12; " +
                        getDifficultyColor(currentQuestion.getDifficulty())
        );

        cardHeader.getChildren().addAll(topicLabel, difficultyBadge);

        Label questionText = new Label(currentQuestion.getQuestionText());
        questionText.setWrapText(true);
        questionText.setFont(Font.font("System", FontWeight.NORMAL, 24));
        questionText.setStyle("-fx-text-fill: #0f172a; -fx-line-spacing: 8px;");

        VBox answerArea = new VBox(15);
        answerArea.setVisible(false);
        answerArea.setManaged(false);
        answerArea.setPadding(new Insets(24));
        answerArea.setStyle(
                "-fx-background-color: #f0fdf4; " +
                        "-fx-border-color: #86efac; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 16; " +
                        "-fx-background-radius: 16;"
        );

        Label answerTitle = new Label("✅ Correct Answer:");
        answerTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        answerTitle.setStyle("-fx-text-fill: #16a34a;");

        Label answerText = new Label(currentQuestion.getAnswer());
        answerText.setWrapText(true);
        answerText.setFont(Font.font(16));
        answerText.setStyle("-fx-text-fill: #0f172a; -fx-line-spacing: 4px;");

        answerArea.getChildren().addAll(answerTitle, answerText);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button showAnswerBtn = new Button("👁️ Show Answer");
        showAnswerBtn.setPrefWidth(220);
        showAnswerBtn.setPrefHeight(50);
        showAnswerBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        showAnswerBtn.setStyle(
                "-fx-background-color: #34aeeb; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );

        Button correctBtn = new Button("✅ I Got It Right");
        correctBtn.setPrefWidth(220);
        correctBtn.setPrefHeight(50);
        correctBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        correctBtn.setVisible(false);
        correctBtn.setManaged(false);
        correctBtn.setStyle(
                "-fx-background-color: #10b981; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 12, 0, 0, 4);"
        );

        Button wrongBtn = new Button("❌ I Got It Wrong");
        wrongBtn.setPrefWidth(220);
        wrongBtn.setPrefHeight(50);
        wrongBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        wrongBtn.setVisible(false);
        wrongBtn.setManaged(false);
        wrongBtn.setStyle(
                "-fx-background-color: #ef4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 12, 0, 0, 4);"
        );

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

        correctBtn.setOnAction(e -> {
            handleAnswer(currentQuestion, true);
        });

        wrongBtn.setOnAction(e -> {
            handleAnswer(currentQuestion, false);
        });

        buttonBox.getChildren().addAll(showAnswerBtn, correctBtn, wrongBtn);

        questionCard.getChildren().addAll(cardHeader, questionText, answerArea, buttonBox);

        questionScreen.getChildren().addAll(progressBox, questionCard);

        ScrollPane scrollPane = new ScrollPane(questionScreen);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private String getDifficultyColor(String difficulty) {
        switch (difficulty.toUpperCase()) {
            case "EASY":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;";
            case "HARD":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;";
            default:
                return "-fx-background-color: #fed7aa; -fx-text-fill: #ea580c;";
        }
    }

    private void handleAnswer(Question question, boolean wasCorrect) {
        boolean saved = studyService.submitAnswer(question.getId(), currentUserId, wasCorrect);

        if (saved) {
            answeredThisSession.add(question.getId()); // Track it
            showAnswerFeedback(wasCorrect);

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                    javafx.util.Duration.seconds(1)
            );
            pause.setOnFinished(e -> nextQuestion());
            pause.play();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save answer", "Please try again.");
        }
    }

    private void showAnswerFeedback(boolean wasCorrect) {
        VBox feedback = new VBox(30);
        feedback.setAlignment(Pos.CENTER);
        feedback.setPadding(new Insets(120));
        feedback.setStyle("-fx-background-color: #f8fafc;");

        Label icon = new Label(wasCorrect ? "🎉" : "💪");
        icon.setFont(Font.font(120));

        Label message = new Label(wasCorrect ? "Great Job!" : "Keep Practicing!");
        message.setFont(Font.font("System", FontWeight.BOLD, 42));
        message.setStyle("-fx-text-fill: " + (wasCorrect ? "#10b981" : "#ef4444") + ";");

        Label detail = new Label(wasCorrect ?
                "You'll see this question again in a few days!" :
                "You'll see this question again tomorrow!");
        detail.setFont(Font.font(18));
        detail.setStyle("-fx-text-fill: #64748b;");

        feedback.getChildren().addAll(icon, message, detail);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(feedback);
    }

    private void nextQuestion() {
        currentQuestionIndex++;

        if (currentQuestionIndex < currentStudyQuestions.size()) {
            showQuestionCard();
        } else {
            showSessionComplete();
        }
    }

    private void showSessionComplete() {
        VBox complete = new VBox(30);
        complete.setAlignment(Pos.CENTER);
        complete.setPadding(new Insets(120));
        complete.setStyle("-fx-background-color: #f8fafc;");

        Label icon = new Label("🎊");
        icon.setFont(Font.font(100));

        Label title = new Label("Session Complete!");
        title.setFont(Font.font("System", FontWeight.BOLD, 42));
        title.setStyle("-fx-text-fill: #0f172a;");

        Label message = new Label("You reviewed " + currentStudyQuestions.size() + " questions today!");
        message.setFont(Font.font(18));
        message.setStyle("-fx-text-fill: #64748b;");

        var stats = studyService.getSessionStats(currentUserId);
        Label statsText = new Label(String.format("Success Rate: %.0f%%", stats.getSuccessRate()));
        statsText.setFont(Font.font("System", FontWeight.BOLD, 24));
        statsText.setStyle("-fx-text-fill: #10b981;");

        // Check if daily goal reached
        int todayReviews = getTodayReviewCount();
        if (todayReviews >= dailyGoalQuestions) {
            Label goalReached = new Label("🎯 Daily Goal Reached!");
            goalReached.setFont(Font.font("System", FontWeight.BOLD, 18));
            goalReached.setStyle("-fx-text-fill: #f59e0b;");
            complete.getChildren().add(goalReached);
        }

        Button dashboardBtn = new Button("🏠 Back to Dashboard");
        dashboardBtn.setPrefWidth(250);
        dashboardBtn.setPrefHeight(54);
        dashboardBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        dashboardBtn.setStyle(
                "-fx-background-color: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 12, 0, 0, 4);"
        );
        dashboardBtn.setOnAction(e -> {
            refreshSidebar();
            showDashboard();
        });

        complete.getChildren().addAll(icon, title, message, statsText, dashboardBtn);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(complete);
    }

    private int getTodayReviewCount() {
        List<Review> allReviews = reviewDAO.getByUserId(currentUserId);
        LocalDate today = LocalDate.now();
        int count = 0;

        for (Review review : allReviews) {
            if (review.getReviewedAt().toLocalDate().equals(today)) {
                count++;
            }
        }

        return count;
    }

    private void showStatistics() {
        VBox statsScreen = new VBox(30);
        statsScreen.setPadding(new Insets(40, 50, 40, 50));
        statsScreen.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Statistics & Analytics");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: #0f172a;");

        var stats = studyService.getSessionStats(currentUserId);
        List<Review> allReviews = reviewDAO.getByUserId(currentUserId);

        HBox overallStats = new HBox(20);

        VBox totalCard = createStatCard(
                "📚",
                "Total Reviews",
                String.valueOf(stats.getTotalReviews()),
                "questions studied",
                "#3b82f6"
        );

        VBox successCard = createStatCard(
                "✅",
                "Success Rate",
                String.format("%.1f%%", stats.getSuccessRate()),
                stats.getCorrectAnswers() + " correct answers",
                "#10b981"
        );

        VBox dueCard = createStatCard(
                "⏰",
                "Due Today",
                String.valueOf(stats.getQuestionsDueToday()),
                "questions need review",
                "#f59e0b"
        );

        VBox streakCard = createStatCard(
                "🔥",
                "Study Streak",
                calculateStreak() + " days",
                "Keep it up!",
                "#ef4444"
        );

        overallStats.getChildren().addAll(totalCard, successCard, dueCard, streakCard);

        Label topicTitle = new Label("Performance by Topic");
        topicTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        topicTitle.setStyle("-fx-text-fill: #0f172a;");

        VBox topicPerformance = new VBox(15);
        topicPerformance.setPadding(new Insets(30));
        topicPerformance.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        List<Topic> topics = topicDAO.getAll();

        if (topics.isEmpty()) {
            Label noData = new Label("No topics yet. Add some questions to see statistics!");
            noData.setFont(Font.font(14));
            noData.setStyle("-fx-text-fill: #64748b;");
            topicPerformance.getChildren().add(noData);
        } else {
            for (Topic topic : topics) {
                VBox topicCard = createTopicPerformanceCard(topic);
                if (topicCard.getChildren().size() > 0) {
                    topicPerformance.getChildren().add(topicCard);
                }
            }
        }

        Label activityTitle = new Label("Recent Activity");
        activityTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        activityTitle.setStyle("-fx-text-fill: #0f172a;");

        VBox activityBox = new VBox(15);
        activityBox.setPadding(new Insets(30));
        activityBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        if (allReviews.isEmpty()) {
            Label noActivity = new Label("No activity yet. Start a study session to see your progress!");
            noActivity.setFont(Font.font(14));
            noActivity.setStyle("-fx-text-fill: #64748b;");
            activityBox.getChildren().add(noActivity);
        } else {
            List<Review> recentReviews = allReviews.subList(0, Math.min(10, allReviews.size()));

            for (Review review : recentReviews) {
                Question question = questionDAO.getById(review.getQuestionId());
                if (question != null) {
                    HBox activityItem = createActivityItem(question, review);
                    activityBox.getChildren().add(activityItem);
                }
            }
        }

        statsScreen.getChildren().addAll(
                title,
                overallStats,
                topicTitle,
                topicPerformance,
                activityTitle,
                activityBox
        );

        ScrollPane scrollPane = new ScrollPane(statsScreen);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createTopicPerformanceCard(Topic topic) {
        VBox card = new VBox(12);

        List<Question> topicQuestions = questionDAO.getByTopicId(topic.getId());

        if (topicQuestions.isEmpty()) {
            return card;
        }

        int totalQuestions = topicQuestions.size();
        int reviewedCount = 0;
        int correctCount = 0;

        for (Question q : topicQuestions) {
            Review latestReview = reviewDAO.getLatestReview(q.getId(), currentUserId);
            if (latestReview != null) {
                reviewedCount++;
                if (latestReview.isWasCorrect()) {
                    correctCount++;
                }
            }
        }

        double successRate = reviewedCount > 0 ? (correctCount * 100.0 / reviewedCount) : 0;
        boolean isWeak = successRate < 70 && reviewedCount > 0;

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label topicName = new Label(topic.getName());
        topicName.setFont(Font.font("System", FontWeight.BOLD, 16));
        topicName.setStyle("-fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stats = new Label(reviewedCount + " / " + totalQuestions + " reviewed");
        stats.setFont(Font.font(13));
        stats.setStyle("-fx-text-fill: #64748b;");

        if (isWeak) {
            Label weakBadge = new Label("⚠️ Needs Practice");
            weakBadge.setPadding(new Insets(4, 10, 4, 10));
            weakBadge.setFont(Font.font("System", FontWeight.BOLD, 11));
            weakBadge.setStyle(
                    "-fx-background-color: #fef2f2; " +
                            "-fx-text-fill: #ef4444; " +
                            "-fx-background-radius: 12;"
            );
            header.getChildren().addAll(topicName, spacer, stats, weakBadge);
        } else {
            header.getChildren().addAll(topicName, spacer, stats);
        }

        ProgressBar progressBar = new ProgressBar(successRate / 100);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);
        progressBar.setStyle(
                isWeak ?
                        "-fx-accent: #ef4444; -fx-background-color: #fee2e2; -fx-background-radius: 5;" :
                        "-fx-accent: #10b981; -fx-background-color: #dcfce7; -fx-background-radius: 5;"
        );

        Label percentage = new Label(String.format("%.0f%% success rate", successRate));
        percentage.setFont(Font.font(13));
        percentage.setStyle("-fx-text-fill: #64748b;");

        card.getChildren().addAll(header, progressBar, percentage);
        card.setStyle("-fx-padding: 10 0 10 0; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        return card;
    }

    private HBox createActivityItem(Question question, Review review) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 0, 12, 0));
        item.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        Label icon = new Label(review.isWasCorrect() ? "✅" : "❌");
        icon.setFont(Font.font(24));

        VBox textBox = new VBox(6);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        String questionText = question.getQuestionText();
        if (questionText.length() > 60) {
            questionText = questionText.substring(0, 60) + "...";
        }

        Label questionLabel = new Label(questionText);
        questionLabel.setFont(Font.font(14));
        questionLabel.setStyle("-fx-text-fill: #0f172a;");
        questionLabel.setWrapText(true);

        Topic topic = topicDAO.getById(question.getTopicId());
        Label topicLabel = new Label("📁 " + (topic != null ? topic.getName() : "Unknown"));
        topicLabel.setFont(Font.font(12));
        topicLabel.setStyle("-fx-text-fill: #64748b;");

        textBox.getChildren().addAll(questionLabel, topicLabel);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        Label nextReview = new Label("Next: " + review.getNextReviewDate().format(formatter));
        nextReview.setFont(Font.font(12));
        nextReview.setStyle("-fx-text-fill: #64748b;");

        item.getChildren().addAll(icon, textBox, nextReview);

        return item;
    }

    private int calculateStreak() {
        List<Review> reviews = reviewDAO.getByUserId(currentUserId);

        if (reviews.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        int streak = 0;

        Set<LocalDate> reviewDays = new HashSet<>();
        for (Review review : reviews) {
            LocalDate reviewDate = review.getReviewedAt().toLocalDate();
            if (reviewDate.isAfter(today.minusDays(30))) {
                reviewDays.add(reviewDate);
            }
        }

        LocalDate checkDate = today;
        while (reviewDays.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private void showSettings() {
        VBox settingsScreen = new VBox(30);
        settingsScreen.setPadding(new Insets(40, 50, 40, 50));
        settingsScreen.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Settings");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: #0f172a;");

        // User Profile Section
        VBox profileSection = new VBox(20);
        profileSection.setPadding(new Insets(30));
        profileSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        Label profileTitle = new Label("👤 User Profile");
        profileTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        profileTitle.setStyle("-fx-text-fill: #0f172a;");

        com.semisaad.smartstudy.model.User user = userDAO.getById(currentUserId);

        HBox userInfo = new HBox(20);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(80, 80);
        avatarContainer.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                        "-fx-background-radius: 40;"
        );
        Label userAvatar = new Label("👤");
        userAvatar.setFont(Font.font(40));
        avatarContainer.getChildren().add(userAvatar);

        VBox userDetails = new VBox(6);
        Label userName = new Label(user != null ? user.getUsername() : "User");
        userName.setFont(Font.font("System", FontWeight.BOLD, 20));
        userName.setStyle("-fx-text-fill: #0f172a;");

        Label userEmail = new Label(user != null && user.getEmail() != null ? user.getEmail() : "No email set");
        userEmail.setFont(Font.font(14));
        userEmail.setStyle("-fx-text-fill: #64748b;");

        userDetails.getChildren().addAll(userName, userEmail);

        userInfo.getChildren().addAll(avatarContainer, userDetails);

        profileSection.getChildren().addAll(profileTitle, userInfo);

        // Progress Section
        VBox statsSection = new VBox(20);
        statsSection.setPadding(new Insets(30));
        statsSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        Label statsTitle = new Label("📊 Your Progress");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        statsTitle.setStyle("-fx-text-fill: #0f172a;");

        var stats = studyService.getSessionStats(currentUserId);
        int totalQuestions = questionDAO.getCount();
        int totalTopics = topicDAO.getAll().size();
        int todayReviews = getTodayReviewCount();

        VBox statsGrid = new VBox(12);

        statsGrid.getChildren().addAll(
                createSettingRow("Total Questions in Library", String.valueOf(totalQuestions)),
                createSettingRow("Total Topics", String.valueOf(totalTopics)),
                createSettingRow("Questions Reviewed", String.valueOf(stats.getTotalReviews())),
                createSettingRow("Success Rate", String.format("%.1f%%", stats.getSuccessRate())),
                createSettingRow("Questions Due Today", String.valueOf(stats.getQuestionsDueToday())),
                createSettingRow("Today's Reviews", String.valueOf(todayReviews)),
                createSettingRow("Study Streak", calculateStreak() + " days")
        );

        statsSection.getChildren().addAll(statsTitle, statsGrid);

        // Preferences Section
        VBox prefsSection = new VBox(20);
        prefsSection.setPadding(new Insets(30));
        prefsSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        Label prefsTitle = new Label("⚙️ Study Preferences");
        prefsTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        prefsTitle.setStyle("-fx-text-fill: #0f172a;");

        // Daily goal
        HBox dailyGoalRow = new HBox(15);
        dailyGoalRow.setAlignment(Pos.CENTER_LEFT);

        Label dailyGoalLabel = new Label("Daily Study Goal:");
        dailyGoalLabel.setFont(Font.font(14));
        dailyGoalLabel.setStyle("-fx-text-fill: #0f172a;");
        dailyGoalLabel.setPrefWidth(200);

        ComboBox<Integer> dailyGoalCombo = new ComboBox<>();
        dailyGoalCombo.getItems().addAll(5, 10, 15, 20, 30, 50);
        dailyGoalCombo.setValue(dailyGoalQuestions);
        dailyGoalCombo.setPrefWidth(120);
        dailyGoalCombo.setPrefHeight(40);
        dailyGoalCombo.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10;"
        );
        dailyGoalCombo.setOnAction(e -> {
            dailyGoalQuestions = dailyGoalCombo.getValue();
            showAlert(Alert.AlertType.INFORMATION, "Settings Updated", "Daily Goal Updated",
                    "Your daily goal has been set to " + dailyGoalQuestions + " questions.");
        });

        Label questionsLabel = new Label("questions/day");
        questionsLabel.setFont(Font.font(14));
        questionsLabel.setStyle("-fx-text-fill: #64748b;");

        dailyGoalRow.getChildren().addAll(dailyGoalLabel, dailyGoalCombo, questionsLabel);

        // Progress indicator for today's goal
        HBox goalProgress = new HBox(15);
        goalProgress.setAlignment(Pos.CENTER_LEFT);

        Label goalProgressLabel = new Label("Today's Progress:");
        goalProgressLabel.setFont(Font.font(14));
        goalProgressLabel.setStyle("-fx-text-fill: #0f172a;");
        goalProgressLabel.setPrefWidth(200);

        ProgressBar todayProgress = new ProgressBar();
        todayProgress.setProgress(dailyGoalQuestions > 0 ? Math.min(1.0, (double) todayReviews / dailyGoalQuestions) : 0);
        todayProgress.setPrefWidth(300);
        todayProgress.setPrefHeight(10);
        todayProgress.setStyle(
                "-fx-accent: #10b981; " +
                        "-fx-background-color: #dcfce7; " +
                        "-fx-background-radius: 5;"
        );

        Label goalText = new Label(todayReviews + " / " + dailyGoalQuestions);
        goalText.setFont(Font.font("System", FontWeight.BOLD, 14));
        goalText.setStyle("-fx-text-fill: " + (todayReviews >= dailyGoalQuestions ? "#10b981" : "#64748b") + ";");

        goalProgress.getChildren().addAll(goalProgressLabel, todayProgress, goalText);

        // Notifications
        HBox notifRow = new HBox(15);
        notifRow.setAlignment(Pos.CENTER_LEFT);

        Label notifLabel = new Label("Daily Reminders:");
        notifLabel.setFont(Font.font(14));
        notifLabel.setStyle("-fx-text-fill: #0f172a;");
        notifLabel.setPrefWidth(200);

        CheckBox notifCheckbox = new CheckBox("Enable daily study reminders");
        notifCheckbox.setSelected(dailyRemindersEnabled);
        notifCheckbox.setFont(Font.font(14));
        notifCheckbox.setOnAction(e -> {
            dailyRemindersEnabled = notifCheckbox.isSelected();
            showAlert(Alert.AlertType.INFORMATION, "Settings Updated", "Reminders " + (dailyRemindersEnabled ? "Enabled" : "Disabled"),
                    "Daily reminders have been " + (dailyRemindersEnabled ? "enabled" : "disabled") + ".");
        });

        notifRow.getChildren().addAll(notifLabel, notifCheckbox);

        prefsSection.getChildren().addAll(prefsTitle, dailyGoalRow, goalProgress, notifRow);

        // About Section
        VBox aboutSection = new VBox(20);
        aboutSection.setPadding(new Insets(30));
        aboutSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );

        Label aboutTitle = new Label("ℹ️ About");
        aboutTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        aboutTitle.setStyle("-fx-text-fill: #0f172a;");

        VBox aboutInfo = new VBox(10);
        aboutInfo.getChildren().addAll(
                createSettingRow("App Name", "Smart Study System"),
                createSettingRow("Version", "1.0.0"),
                createSettingRow("Algorithm", "SuperMemo 2 (SM-2)"),
                createSettingRow("Database", "PostgreSQL 16"),
                createSettingRow("Framework", "JavaFX 21"),
                createSettingRow("Developer", "Saad Khan")
        );

        aboutSection.getChildren().addAll(aboutTitle, aboutInfo);

        // Action buttons
        HBox actionButtons = new HBox(15);

        Button resetBtn = new Button("🔄 Reset All Progress");
        resetBtn.setPrefHeight(44);
        resetBtn.setPrefWidth(200);
        resetBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        resetBtn.setStyle(
                "-fx-background-color: #fef2f2; " +
                        "-fx-text-fill: #ef4444; " +
                        "-fx-border-color: #ef4444; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand;"
        );
        resetBtn.setOnAction(e -> showResetConfirmation());

        Button exportBtn = new Button("📥 Export Data");
        exportBtn.setPrefHeight(44);
        exportBtn.setPrefWidth(180);
        exportBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        exportBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-border-color: #3b82f6; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand;"
        );
        exportBtn.setOnAction(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Export Data", "Feature Coming Soon!",
                    "Data export functionality will be available in a future update.");
        });

        actionButtons.getChildren().addAll(resetBtn, exportBtn);

        settingsScreen.getChildren().addAll(
                title,
                profileSection,
                statsSection,
                prefsSection,
                aboutSection,
                actionButtons
        );

        ScrollPane scrollPane = new ScrollPane(settingsScreen);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private HBox createSettingRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        HBox.setHgrow(row, Priority.ALWAYS);

        Label labelText = new Label(label);
        labelText.setFont(Font.font(14));
        labelText.setStyle("-fx-text-fill: #64748b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueText = new Label(value);
        valueText.setFont(Font.font("System", FontWeight.BOLD, 14));
        valueText.setStyle("-fx-text-fill: #0f172a;");

        row.getChildren().addAll(labelText, spacer, valueText);

        return row;
    }

    private void showResetConfirmation() {
        Alert confirmation = new Alert(Alert.AlertType.WARNING);
        confirmation.setTitle("Reset Progress");
        confirmation.setHeaderText("⚠️ Warning: This Cannot Be Undone!");
        confirmation.setContentText(
                "This will delete ALL your review history and progress.\n" +
                        "Your questions and topics will remain.\n\n" +
                        "Are you absolutely sure?"
        );

        ButtonType yesButton = new ButtonType("Yes, Reset Everything");
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(yesButton, noButton);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                List<Review> userReviews = reviewDAO.getByUserId(currentUserId);

                int deletedCount = 0;
                for (Review review : userReviews) {
                    if (reviewDAO.delete(review.getId())) {
                        deletedCount++;
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Reset Complete", "Progress Reset!",
                        "Deleted " + deletedCount + " review records. Your learning journey starts fresh!");

                refreshSidebar();
                showSettings();
            }
        });
    }

    private void setActiveNavButton(Button button) {
        if (activeNavButton != null) {
            String baseStyle =
                    "-fx-background-radius: 12; " +
                            "-fx-cursor: hand; " +
                            "-fx-padding: 0 0 0 20;";
            activeNavButton.setStyle(baseStyle +
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: rgba(255,255,255,0.7);"
            );
        }

        String baseStyle =
                "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0 0 0 20;";
        button.setStyle(baseStyle +
                "-fx-background-color: rgba(255,255,255,0.15); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;"
        );

        activeNavButton = button;
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}