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
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;

import com.semisaad.smartstudy.service.StudySessionService;
import com.semisaad.smartstudy.dao.TopicDAO;
import com.semisaad.smartstudy.dao.QuestionDAO;
import com.semisaad.smartstudy.dao.UserDAO;
import com.semisaad.smartstudy.dao.ReviewDAO;
import com.semisaad.smartstudy.model.Review;
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
        VBox topicsScreen = new VBox(20);
        topicsScreen.setPadding(new Insets(30, 40, 30, 40));

        // Header with Add button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label title = new Label("Topics Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addTopicBtn = new Button("➕ Add Topic");
        addTopicBtn.setPrefHeight(40);
        addTopicBtn.setPrefWidth(150);
        addTopicBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
        addTopicBtn.setOnAction(e -> showAddTopicDialog());

        header.getChildren().addAll(title, spacer, addTopicBtn);

        // Topics grid
        FlowPane topicsGrid = new FlowPane(20, 20);

        List<Topic> topics = topicDAO.getAll();
        String[] colors = {"yellow", "blue", "purple", "cyan", "pink", "green", "orange", "red"};
        String[] icons = {"📦", "🔄", "🎯", "🗄️", "💻", "🌐", "📚", "🧠"};

        if (topics.isEmpty()) {
            Label noTopics = new Label("No topics yet. Click 'Add Topic' to create your first topic!");
            noTopics.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px;");
            noTopics.setPadding(new Insets(50));
            topicsGrid.getChildren().add(noTopics);
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
        scrollPane.setStyle("-fx-background-color: #f5f7fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createManageableTopicCard(Topic topic, int questionCount, String icon, String color) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("topic-card", "topic-card-" + color);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.setPrefHeight(200);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        );

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #3b82f6; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 10, 0, 0, 4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        ));

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(40));

        // Topic name
        Label nameLabel = new Label(topic.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(210);

        // Question count
        Label countLabel = new Label(questionCount + " questions");
        countLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        // Description (truncated)
        String desc = topic.getDescription();
        if (desc != null && !desc.isEmpty()) {
            if (desc.length() > 40) {
                desc = desc.substring(0, 40) + "...";
            }
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(210);
            card.getChildren().add(descLabel);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✏️ Edit");
        editBtn.setPrefWidth(100);
        editBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-border-color: #3b82f6; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 12px;"
        );
        editBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            showEditTopicDialog(topic);
        });

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle(
                "-fx-background-color: #fef2f2; " +
                        "-fx-text-fill: #ef4444; " +
                        "-fx-border-color: #ef4444; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 12px;"
        );
        deleteBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            deleteTopic(topic, questionCount);
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(iconLabel, nameLabel, countLabel, spacer, actions);

        return card;
    }

    private void showAddTopicDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Topic");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setPrefWidth(600);
        dialogContent.setPrefHeight(400);

        Label title = new Label("➕ Add New Topic");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Topic name
        Label nameLabel = new Label("Topic Name:");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Data Structures, Algorithms...");
        nameField.setPrefHeight(40);
        nameField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Description
        Label descLabel = new Label("Description (optional):");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea descField = new TextArea();
        descField.setPromptText("Brief description of this topic...");
        descField.setPrefRowCount(4);
        descField.setWrapText(true);
        descField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #f3f4f6; " +
                        "-fx-text-fill: #1f2937; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Create Topic");
        saveBtn.setPrefWidth(150);
        saveBtn.setPrefHeight(40);
        saveBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String description = descField.getText().trim();

            if (name.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText("Topic Name Required");
                alert.setContentText("Please enter a topic name.");
                alert.showAndWait();
                return;
            }

            Topic newTopic = new Topic(name, description);
            boolean saved = topicDAO.insert(newTopic);

            if (saved) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Topic Created!");
                alert.setContentText("The topic '" + name + "' has been created successfully.");
                alert.showAndWait();

                dialog.close();
                showTopics(); // Refresh
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Save Failed");
                alert.setContentText("Could not create the topic. It might already exist.");
                alert.showAndWait();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(
                title,
                nameLabel, nameField,
                descLabel, descField,
                buttonBox
        );

        Scene dialogScene = new Scene(dialogContent);
        dialog.setScene(dialogScene);
        dialog.setMinWidth(650);
        dialog.setMinHeight(450);
        dialog.showAndWait();
    }

    private void showEditTopicDialog(Topic topic) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Topic");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setPrefWidth(600);
        dialogContent.setPrefHeight(400);

        Label title = new Label("✏️ Edit Topic");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Topic name
        Label nameLabel = new Label("Topic Name:");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextField nameField = new TextField(topic.getName());
        nameField.setPrefHeight(40);
        nameField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea descField = new TextArea(topic.getDescription());
        descField.setPrefRowCount(4);
        descField.setWrapText(true);
        descField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #f3f4f6; " +
                        "-fx-text-fill: #1f2937; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setPrefWidth(150);
        saveBtn.setPrefHeight(40);
        saveBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> {
            topic.setName(nameField.getText().trim());
            topic.setDescription(descField.getText().trim());

            boolean updated = topicDAO.update(topic);

            if (updated) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Topic Updated!");
                alert.setContentText("The topic has been updated successfully.");
                alert.showAndWait();

                dialog.close();
                showTopics(); // Refresh
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(
                title,
                nameLabel, nameField,
                descLabel, descField,
                buttonBox
        );

        Scene dialogScene = new Scene(dialogContent);
        dialog.setScene(dialogScene);
        dialog.setMinWidth(650);
        dialog.setMinHeight(450);
        dialog.showAndWait();
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
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Topic Deleted");
                    alert.setContentText("The topic has been deleted successfully.");
                    alert.showAndWait();

                    showTopics(); // Refresh
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Delete Failed");
                    alert.setContentText("Could not delete the topic. Please try again.");
                    alert.showAndWait();
                }
            }
        });
    }

    private void showQuestions() {
        VBox questionsScreen = new VBox(20);
        questionsScreen.setPadding(new Insets(30, 40, 30, 40));

        // Header with Add button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label title = new Label("Questions Library");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addQuestionBtn = new Button("➕ Add Question");
        addQuestionBtn.setPrefHeight(40);
        addQuestionBtn.setPrefWidth(150);
        addQuestionBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );
        addQuestionBtn.setOnAction(e -> showAddQuestionDialog());

        header.getChildren().addAll(title, spacer, addQuestionBtn);

        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search questions...");
        searchField.setPrefWidth(300);
        searchField.setPrefHeight(40);
        searchField.setStyle(
                "-fx-background-radius: 10; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 10;"
        );

        ComboBox<String> topicFilter = new ComboBox<>();
        topicFilter.getItems().add("All Topics");
        List<Topic> topics = topicDAO.getAll();
        for (Topic topic : topics) {
            topicFilter.getItems().add(topic.getName());
        }
        topicFilter.setValue("All Topics");
        topicFilter.setPrefHeight(40);
        topicFilter.setStyle("-fx-background-radius: 10;");

        searchBox.getChildren().addAll(searchField, topicFilter);

        // Questions table
        VBox tableContainer = new VBox();
        tableContainer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        // Table header
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(15, 20, 15, 20));
        tableHeader.setStyle("-fx-background-color: #f9fafb; -fx-border-radius: 12 12 0 0; -fx-background-radius: 12 12 0 0;");
        tableHeader.setSpacing(20);

        Label headerQuestion = new Label("Question");
        headerQuestion.setPrefWidth(350);
        headerQuestion.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerQuestion.setStyle("-fx-text-fill: #6b7280;");

        Label headerTopic = new Label("Topic");
        headerTopic.setPrefWidth(150);
        headerTopic.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerTopic.setStyle("-fx-text-fill: #6b7280;");

        Label headerDifficulty = new Label("Difficulty");
        headerDifficulty.setPrefWidth(100);
        headerDifficulty.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerDifficulty.setStyle("-fx-text-fill: #6b7280;");

        Label headerActions = new Label("Actions");
        headerActions.setPrefWidth(150);
        headerActions.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerActions.setStyle("-fx-text-fill: #6b7280;");

        tableHeader.getChildren().addAll(headerQuestion, headerTopic, headerDifficulty, headerActions);

        // Table rows container
        VBox tableRows = new VBox();

        // Get all questions
        List<Question> questions = questionDAO.getAll();

        for (Question q : questions) {
            Topic topic = topicDAO.getById(q.getTopicId());
            HBox row = createQuestionRow(q, topic);
            tableRows.getChildren().add(row);
        }

        // Wrap in ScrollPane
        ScrollPane tableScroll = new ScrollPane(tableRows);
        tableScroll.setFitToWidth(true);
        tableScroll.setPrefHeight(400);
        tableScroll.setStyle("-fx-background-color: white; -fx-background: white;");

        tableContainer.getChildren().addAll(tableHeader, tableScroll);

        // Search functionality
        searchField.textProperty().addListener((obs, old, newVal) -> {
            updateQuestionsList(tableRows, newVal, topicFilter.getValue());
        });

        topicFilter.setOnAction(e -> {
            updateQuestionsList(tableRows, searchField.getText(), topicFilter.getValue());
        });

        questionsScreen.getChildren().addAll(header, searchBox, tableContainer);

        ScrollPane mainScroll = new ScrollPane(questionsScreen);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background-color: #f5f7fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(mainScroll);
    }

    private HBox createQuestionRow(Question q, Topic topic) {
        HBox row = new HBox();
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setSpacing(20);
        row.setStyle(
                "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-color: white;"
        );

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-color: #f9fafb;"
        ));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-color: white;"
        ));

        // Question text (truncated)
        Label questionLabel = new Label(q.getQuestionText());
        questionLabel.setPrefWidth(350);
        questionLabel.setWrapText(true);
        questionLabel.setMaxHeight(60);
        questionLabel.setFont(Font.font(14));

        // Topic
        Label topicLabel = new Label(topic != null ? topic.getName() : "Unknown");
        topicLabel.setPrefWidth(150);
        topicLabel.setStyle("-fx-text-fill: #6b7280;");

        // Difficulty badge
        Label difficultyLabel = new Label(q.getDifficulty());
        difficultyLabel.setPrefWidth(100);
        difficultyLabel.setPadding(new Insets(4, 10, 4, 10));
        difficultyLabel.setStyle(
                "-fx-background-radius: 12; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        getDifficultyColor(q.getDifficulty())
        );

        // Action buttons
        HBox actions = new HBox(8);
        actions.setPrefWidth(150);

        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-border-color: #3b82f6; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 12px;"
        );
        editBtn.setOnAction(e -> showEditQuestionDialog(q));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle(
                "-fx-background-color: #fef2f2; " +
                        "-fx-text-fill: #ef4444; " +
                        "-fx-border-color: #ef4444; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 12px;"
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

            // Filter by search text
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    q.getQuestionText().toLowerCase().contains(searchText.toLowerCase()) ||
                    q.getAnswer().toLowerCase().contains(searchText.toLowerCase());

            // Filter by topic
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

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setPrefHeight(700);
        dialogContent.setPrefWidth(650);

        Label title = new Label("➕ Add New Question");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Question text
        Label questionLabel = new Label("Question:");
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea questionField = new TextArea();
        questionField.setPromptText("Enter your question here...");
        questionField.setPrefRowCount(3);
        questionField.setWrapText(true);
        questionField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Answer
        Label answerLabel = new Label("Answer:");
        answerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea answerField = new TextArea();
        answerField.setPromptText("Enter the correct answer...");
        answerField.setPrefRowCount(3);
        answerField.setWrapText(true);
        answerField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Topic
        Label topicLabel = new Label("Topic:");
        topicLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        ComboBox<String> topicCombo = new ComboBox<>();
        List<Topic> topics = topicDAO.getAll();
        for (Topic t : topics) {
            topicCombo.getItems().add(t.getName());
        }
        if (!topics.isEmpty()) {
            topicCombo.setValue(topics.get(0).getName());
        }
        topicCombo.setPrefWidth(300);
        topicCombo.setStyle("-fx-background-radius: 8;");

        // Difficulty
        Label difficultyLabel = new Label("Difficulty:");
        difficultyLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("EASY", "MEDIUM", "HARD");
        difficultyCombo.setValue("MEDIUM");
        difficultyCombo.setPrefWidth(300);
        difficultyCombo.setStyle("-fx-background-radius: 8;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #f3f4f6; " +
                        "-fx-text-fill: #1f2937; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Question");
        saveBtn.setPrefWidth(150);
        saveBtn.setPrefHeight(40);
        saveBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> {
            String questionText = questionField.getText().trim();
            String answer = answerField.getText().trim();
            String topicName = topicCombo.getValue();
            String difficulty = difficultyCombo.getValue();

            if (questionText.isEmpty() || answer.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText("Missing Information");
                alert.setContentText("Please fill in both question and answer fields.");
                alert.showAndWait();
                return;
            }

            // Find topic ID
            int topicId = 0;
            for (Topic t : topics) {
                if (t.getName().equals(topicName)) {
                    topicId = t.getId();
                    break;
                }
            }

            // Create and save question
            Question newQuestion = new Question(questionText, answer, topicId, difficulty);
            boolean saved = questionDAO.insert(newQuestion);

            if (saved) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Question Added!");
                alert.setContentText("The question has been added successfully.");
                alert.showAndWait();

                dialog.close();
                showQuestions(); // Refresh the list
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Save Failed");
                alert.setContentText("Could not save the question. Please try again.");
                alert.showAndWait();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(
                title,
                questionLabel, questionField,
                answerLabel, answerField,
                topicLabel, topicCombo,
                difficultyLabel, difficultyCombo,
                buttonBox
        );

        Scene dialogScene = new Scene(dialogContent);
        dialog.setScene(dialogScene);
        dialog.setMinWidth(750);
        dialog.setMinHeight(700);
        dialog.showAndWait();
    }

    private void showEditQuestionDialog(Question question) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Question");
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setPrefHeight(700);
        dialogContent.setPrefWidth(650);

        Label title = new Label("✏️ Edit Question");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Question text
        Label questionLabel = new Label("Question:");
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea questionField = new TextArea(question.getQuestionText());
        questionField.setPrefRowCount(3);
        questionField.setWrapText(true);
        questionField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Answer
        Label answerLabel = new Label("Answer:");
        answerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea answerField = new TextArea(question.getAnswer());
        answerField.setPrefRowCount(3);
        answerField.setWrapText(true);
        answerField.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Topic
        Label topicLabel = new Label("Topic:");
        topicLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        ComboBox<String> topicCombo = new ComboBox<>();
        List<Topic> topics = topicDAO.getAll();
        Topic currentTopic = topicDAO.getById(question.getTopicId());
        for (Topic t : topics) {
            topicCombo.getItems().add(t.getName());
        }
        topicCombo.setValue(currentTopic != null ? currentTopic.getName() : topics.get(0).getName());
        topicCombo.setPrefWidth(300);

        // Difficulty
        Label difficultyLabel = new Label("Difficulty:");
        difficultyLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("EASY", "MEDIUM", "HARD");
        difficultyCombo.setValue(question.getDifficulty());
        difficultyCombo.setPrefWidth(300);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #f3f4f6; " +
                        "-fx-text-fill: #1f2937; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setPrefWidth(150);
        saveBtn.setPrefHeight(40);
        saveBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> {
            question.setQuestionText(questionField.getText().trim());
            question.setAnswer(answerField.getText().trim());
            question.setDifficulty(difficultyCombo.getValue());

            // Find topic ID
            for (Topic t : topics) {
                if (t.getName().equals(topicCombo.getValue())) {
                    question.setTopicId(t.getId());
                    break;
                }
            }

            boolean updated = questionDAO.update(question);

            if (updated) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Question Updated!");
                alert.setContentText("The question has been updated successfully.");
                alert.showAndWait();

                dialog.close();
                showQuestions(); // Refresh
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        dialogContent.getChildren().addAll(
                title,
                questionLabel, questionField,
                answerLabel, answerField,
                topicLabel, topicCombo,
                difficultyLabel, difficultyCombo,
                buttonBox
        );

        Scene dialogScene = new Scene(dialogContent);
        dialog.setScene(dialogScene);
        dialog.setMinWidth(750);
        dialog.setMinHeight(700);
        dialog.showAndWait();
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
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Question Deleted");
                    alert.setContentText("The question has been deleted successfully.");
                    alert.showAndWait();

                    showQuestions(); // Refresh
                }
            }
        });
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
        VBox statsScreen = new VBox(30);
        statsScreen.setPadding(new Insets(30, 40, 30, 40));

        // Header
        Label title = new Label("Statistics & Analytics");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        // Get statistics
        var stats = studyService.getSessionStats(currentUserId);
        List<Review> allReviews = new ReviewDAO().getByUserId(currentUserId);

        // Overall Stats Cards
        HBox overallStats = new HBox(20);

        VBox totalCard = createStatCard(
                "📚 Total Reviews",
                String.valueOf(stats.getTotalReviews()),
                "questions studied"
        );

        VBox successCard = createStatCard(
                "✅ Success Rate",
                String.format("%.1f%%", stats.getSuccessRate()),
                stats.getCorrectAnswers() + " correct answers"
        );

        VBox dueCard = createStatCard(
                "⏰ Due Today",
                String.valueOf(stats.getQuestionsDueToday()),
                "questions need review"
        );

        VBox streakCard = createStatCard(
                "🔥 Study Streak",
                calculateStreak(allReviews) + " days",
                "Keep it up!"
        );

        overallStats.getChildren().addAll(totalCard, successCard, dueCard, streakCard);

        // Performance by Topic Section
        Label topicTitle = new Label("Performance by Topic");
        topicTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        topicTitle.setStyle("-fx-text-fill: #1f2937;");

        VBox topicPerformance = new VBox(15);
        topicPerformance.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 30; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        List<Topic> topics = topicDAO.getAll();

        if (topics.isEmpty()) {
            Label noData = new Label("No topics yet. Add some questions to see statistics!");
            noData.setStyle("-fx-text-fill: #6b7280;");
            topicPerformance.getChildren().add(noData);
        } else {
            for (Topic topic : topics) {
                VBox topicCard = createTopicPerformanceCard(topic);
                topicPerformance.getChildren().add(topicCard);
            }
        }

        // Recent Activity Section
        Label activityTitle = new Label("Recent Activity");
        activityTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        activityTitle.setStyle("-fx-text-fill: #1f2937;");

        VBox activityBox = new VBox(15);
        activityBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 30; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        if (allReviews.isEmpty()) {
            Label noActivity = new Label("No activity yet. Start a study session to see your progress!");
            noActivity.setStyle("-fx-text-fill: #6b7280;");
            activityBox.getChildren().add(noActivity);
        } else {
            // Show last 5 reviews
            List<Review> recentReviews = allReviews.subList(0, Math.min(5, allReviews.size()));

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
        scrollPane.setStyle("-fx-background-color: #f5f7fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createTopicPerformanceCard(Topic topic) {
        VBox card = new VBox(10);

        // Get questions for this topic
        List<Question> topicQuestions = questionDAO.getByTopicId(topic.getId());

        if (topicQuestions.isEmpty()) {
            // Skip topics with no questions
            return card;
        }

        // Calculate performance
        int totalQuestions = topicQuestions.size();
        int reviewedCount = 0;
        int correctCount = 0;

        ReviewDAO reviewDAO = new ReviewDAO();
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

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);

        Label topicName = new Label(topic.getName());
        topicName.setFont(Font.font("System", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stats = new Label(reviewedCount + " / " + totalQuestions + " reviewed");
        stats.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        // Weak topic indicator
        if (isWeak) {
            Label weakBadge = new Label("⚠️ Needs Practice");
            weakBadge.setStyle(
                    "-fx-background-color: #fef2f2; " +
                            "-fx-text-fill: #ef4444; " +
                            "-fx-padding: 4 10 4 10; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold;"
            );
            header.getChildren().addAll(topicName, spacer, stats, weakBadge);
        } else {
            header.getChildren().addAll(topicName, spacer, stats);
        }

        // Progress bar
        ProgressBar progressBar = new ProgressBar(successRate / 100);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);
        progressBar.setStyle(
                isWeak ?
                        "-fx-accent: #ef4444;" : // Red for weak topics
                        "-fx-accent: #10b981;"   // Green for good topics
        );

        // Percentage label
        Label percentage = new Label(String.format("%.0f%% success rate", successRate));
        percentage.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        card.getChildren().addAll(header, progressBar, percentage);

        return card;
    }

    private HBox createActivityItem(Question question, Review review) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 0, 10, 0));
        item.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        // Icon
        Label icon = new Label(review.isWasCorrect() ? "✅" : "❌");
        icon.setFont(Font.font(20));

        // Question text
        VBox textBox = new VBox(5);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        String questionText = question.getQuestionText();
        if (questionText.length() > 60) {
            questionText = questionText.substring(0, 60) + "...";
        }

        Label questionLabel = new Label(questionText);
        questionLabel.setFont(Font.font(14));
        questionLabel.setWrapText(true);

        Topic topic = topicDAO.getById(question.getTopicId());
        Label topicLabel = new Label("📁 " + (topic != null ? topic.getName() : "Unknown"));
        topicLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        textBox.getChildren().addAll(questionLabel, topicLabel);

        // Next review date
        Label nextReview = new Label("Next: " + review.getNextReviewDate().toString());
        nextReview.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        item.getChildren().addAll(icon, textBox, nextReview);

        return item;
    }

    private int calculateStreak(List<Review> reviews) {
        if (reviews.isEmpty()) return 0;

        // Simple streak calculation - consecutive days with reviews
        java.time.LocalDate today = java.time.LocalDate.now();
        int streak = 0;

        // Count unique days with reviews in the last 30 days
        java.util.Set<java.time.LocalDate> reviewDays = new java.util.HashSet<>();
        for (Review review : reviews) {
            java.time.LocalDate reviewDate = review.getReviewedAt().toLocalDate();
            if (reviewDate.isAfter(today.minusDays(30))) {
                reviewDays.add(reviewDate);
            }
        }

        // Calculate consecutive days from today backwards
        java.time.LocalDate checkDate = today;
        while (reviewDays.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private void showSettings() {
        VBox settingsScreen = new VBox(30);
        settingsScreen.setPadding(new Insets(30, 40, 30, 40));

        // Header
        Label title = new Label("Settings");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        // User Profile Section
        VBox profileSection = new VBox(15);
        profileSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        Label profileTitle = new Label("👤 User Profile");
        profileTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        // User info
        UserDAO userDAO = new UserDAO();
        com.semisaad.smartstudy.model.User user = userDAO.getById(currentUserId);

        HBox userInfo = new HBox(20);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        Label userAvatar = new Label("👤");
        userAvatar.setFont(Font.font(50));
        userAvatar.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #3b82f6, #8b5cf6); " +
                        "-fx-background-radius: 50; " +
                        "-fx-min-width: 80; " +
                        "-fx-min-height: 80; " +
                        "-fx-alignment: center;"
        );

        VBox userDetails = new VBox(5);
        Label userName = new Label(user != null ? user.getUsername() : "User");
        userName.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label userEmail = new Label(user != null && user.getEmail() != null ? user.getEmail() : "No email set");
        userEmail.setStyle("-fx-text-fill: #6b7280;");

        userDetails.getChildren().addAll(userName, userEmail);

        userInfo.getChildren().addAll(userAvatar, userDetails);

        profileSection.getChildren().addAll(profileTitle, userInfo);

        // App Statistics Section
        VBox statsSection = new VBox(15);
        statsSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        Label statsTitle = new Label("📊 Your Progress");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        var stats = studyService.getSessionStats(currentUserId);
        int totalQuestions = questionDAO.getCount();
        int totalTopics = topicDAO.getAll().size();

        VBox statsGrid = new VBox(10);

        statsGrid.getChildren().addAll(
                createSettingRow("Total Questions in Library", String.valueOf(totalQuestions)),
                createSettingRow("Total Topics", String.valueOf(totalTopics)),
                createSettingRow("Questions Reviewed", String.valueOf(stats.getTotalReviews())),
                createSettingRow("Success Rate", String.format("%.1f%%", stats.getSuccessRate())),
                createSettingRow("Questions Due Today", String.valueOf(stats.getQuestionsDueToday()))
        );

        statsSection.getChildren().addAll(statsTitle, statsGrid);

        // Preferences Section
        VBox prefsSection = new VBox(15);
        prefsSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        Label prefsTitle = new Label("⚙️ Preferences");
        prefsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Daily goal
        HBox dailyGoalRow = new HBox(15);
        dailyGoalRow.setAlignment(Pos.CENTER_LEFT);

        Label dailyGoalLabel = new Label("Daily Study Goal:");
        dailyGoalLabel.setFont(Font.font(14));
        dailyGoalLabel.setPrefWidth(200);

        ComboBox<String> dailyGoalCombo = new ComboBox<>();
        dailyGoalCombo.getItems().addAll("5 questions", "10 questions", "15 questions", "20 questions", "30 questions");
        dailyGoalCombo.setValue("10 questions");
        dailyGoalCombo.setPrefWidth(150);

        dailyGoalRow.getChildren().addAll(dailyGoalLabel, dailyGoalCombo);

        // Notifications (placeholder)
        HBox notifRow = new HBox(15);
        notifRow.setAlignment(Pos.CENTER_LEFT);

        Label notifLabel = new Label("Daily Reminder:");
        notifLabel.setFont(Font.font(14));
        notifLabel.setPrefWidth(200);

        CheckBox notifCheckbox = new CheckBox("Enable daily study reminders");
        notifCheckbox.setSelected(true);

        notifRow.getChildren().addAll(notifLabel, notifCheckbox);

        prefsSection.getChildren().addAll(prefsTitle, dailyGoalRow, notifRow);

        // About Section
        VBox aboutSection = new VBox(15);
        aboutSection.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        Label aboutTitle = new Label("ℹ️ About");
        aboutTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        VBox aboutInfo = new VBox(8);
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
        resetBtn.setPrefHeight(40);
        resetBtn.setStyle(
                "-fx-background-color: #fef2f2; " +
                        "-fx-text-fill: #ef4444; " +
                        "-fx-border-color: #ef4444; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        resetBtn.setOnAction(e -> showResetConfirmation());

        Button exportBtn = new Button("📥 Export Data");
        exportBtn.setPrefHeight(40);
        exportBtn.setStyle(
                "-fx-background-color: #eff6ff; " +
                        "-fx-text-fill: #3b82f6; " +
                        "-fx-border-color: #3b82f6; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        exportBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Data");
            alert.setHeaderText("Feature Coming Soon!");
            alert.setContentText("Data export functionality will be available in a future update.");
            alert.showAndWait();
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
        scrollPane.setStyle("-fx-background-color: #f5f7fa;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(scrollPane);
    }

    private HBox createSettingRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        HBox.setHgrow(row, Priority.ALWAYS);

        Label labelText = new Label(label);
        labelText.setFont(Font.font(14));
        labelText.setStyle("-fx-text-fill: #6b7280;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueText = new Label(value);
        valueText.setFont(Font.font("System", FontWeight.BOLD, 14));
        valueText.setStyle("-fx-text-fill: #1f2937;");

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
                // Delete all reviews for this user
                ReviewDAO reviewDAO = new ReviewDAO();
                List<Review> userReviews = reviewDAO.getByUserId(currentUserId);

                int deletedCount = 0;
                for (Review review : userReviews) {
                    if (reviewDAO.delete(review.getId())) {
                        deletedCount++;
                    }
                }

                Alert result = new Alert(Alert.AlertType.INFORMATION);
                result.setTitle("Reset Complete");
                result.setHeaderText("Progress Reset!");
                result.setContentText("Deleted " + deletedCount + " review records.\nYour learning journey starts fresh!");
                result.showAndWait();

                // Refresh the settings screen to show updated stats
                showSettings();
            }
        });
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
