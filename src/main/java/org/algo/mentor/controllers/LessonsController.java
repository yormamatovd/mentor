package org.algo.mentor.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.Group;
import org.algo.mentor.models.Lesson;
import org.algo.mentor.models.LessonDetail;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.LessonService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LessonsController implements NavigableController {
    @FXML private VBox groupSelectionView;
    @FXML private FlowPane groupsFlowPane;
    
    @FXML private VBox lessonHistoryView;
    @FXML private Label historyGroupTitleLabel;
    @FXML private VBox historyRowsContainer;

    @FXML private VBox lessonDetailView;
    @FXML private Label groupTitleLabel;
    @FXML private Label dateLabel;
    @FXML private StackPane autoSaveToggle;
    @FXML private Region toggleBackground;
    @FXML private Region toggleThumb;
    @FXML private Region autoSaveIndicator;
    @FXML private VBox lessonRowsContainer;
    @FXML private Button enableEditButton;
    @FXML private Button saveButton;
    @FXML private HBox autoSaveContainer;
    @FXML private HBox deleteContainer;
    @FXML private Label deleteWarningLabel;
    @FXML private Button deleteButton;
    
    @FXML private Label homeworkSortIcon;
    @FXML private Label testScoreSortIcon;
    
    private NavigationController navigationController;
    private Group selectedGroup;
    private Lesson currentLesson;
    private Timeline autoSaveTimeline;
    private FadeTransition idlePulse;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private List<LessonDetail> currentDetails = new ArrayList<>();
    private boolean isEditable = true;
    private boolean fromHistory = false;
    private String currentSortField = "name";
    private boolean sortAscending = true;
    private boolean isDeleteConfirmed = false;
    private boolean autoSaveEnabled = true;

    @FXML
    public void initialize() {
        loadGroups();
        
        if (deleteContainer != null) {
            deleteContainer.managedProperty().bind(deleteContainer.visibleProperty());
        }
        if (deleteWarningLabel != null) {
            deleteWarningLabel.managedProperty().bind(deleteWarningLabel.visibleProperty());
        }

        if (enableEditButton != null) {
            enableEditButton.managedProperty().bind(enableEditButton.visibleProperty());
        }
        if (autoSaveContainer != null) {
            autoSaveContainer.managedProperty().bind(autoSaveContainer.visibleProperty());
        }
        if (autoSaveToggle != null) {
            autoSaveToggle.managedProperty().bind(autoSaveToggle.visibleProperty());
        }
        if (saveButton != null) {
            saveButton.managedProperty().bind(saveButton.visibleProperty());
        }
        
        setupToggleHoverEffects();
        updateToggleState(true);
    }
    
    private void setupToggleHoverEffects() {
        if (autoSaveToggle == null || toggleThumb == null) return;
        
        String baseThumbStyle = "-fx-background-color: white; " +
                               "-fx-background-radius: 11; " +
                               "-fx-min-width: 22; -fx-max-width: 22; " +
                               "-fx-min-height: 22; -fx-max-height: 22;";
        
        autoSaveToggle.setOnMouseEntered(e -> {
            if (toggleThumb != null) {
                toggleThumb.setStyle(baseThumbStyle + 
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 6, 0.5, 0, 3);");
            }
        });
        
        autoSaveToggle.setOnMouseExited(e -> {
            if (toggleThumb != null) {
                toggleThumb.setStyle(baseThumbStyle + 
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0.3, 0, 2);");
            }
        });
    }
    
    @FXML
    private void onToggleAutoSave() {
        autoSaveEnabled = !autoSaveEnabled;
        updateToggleState(true);
        updateIndicatorState();
    }
    
    private void updateToggleState(boolean animate) {
        if (toggleBackground == null || toggleThumb == null) return;
        
        String bgColor = autoSaveEnabled ? "linear-gradient(to right, #38a169 0%, #48bb78 100%)" : "#e2e8f0";
        String shadowColor = autoSaveEnabled 
            ? "dropshadow(three-pass-box, rgba(56,161,105,0.3), 8, 0.4, 0, 2)" 
            : "dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0.2, 0, 1)";
        double targetX = autoSaveEnabled ? 12 : -12;
        
        String bgStyle = String.format(
            "-fx-background-color: %s; -fx-background-radius: 14; " +
            "-fx-min-width: 52; -fx-max-width: 52; " +
            "-fx-min-height: 28; -fx-max-height: 28; " +
            "-fx-effect: %s;",
            bgColor, shadowColor
        );
        toggleBackground.setStyle(bgStyle);
        
        String thumbStyle = "-fx-background-color: white; " +
                           "-fx-background-radius: 11; " +
                           "-fx-min-width: 22; -fx-max-width: 22; " +
                           "-fx-min-height: 22; -fx-max-height: 22; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0.3, 0, 2);";
        toggleThumb.setStyle(thumbStyle);
        
        if (animate) {
            TranslateTransition slideTransition = new TranslateTransition(Duration.millis(250), toggleThumb);
            slideTransition.setToX(targetX);
            slideTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
            slideTransition.play();
        } else {
            toggleThumb.setTranslateX(targetX);
        }
    }

    private void autoSaveIfEnabled() {
        if (isEditable && autoSaveEnabled) {
            if (autoSaveTimeline != null) {
                autoSaveTimeline.stop();
            }
            
            autoSaveTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                onSaveLesson();
            }));
            autoSaveTimeline.setCycleCount(1);
            autoSaveTimeline.play();
        }
    }
    
    private void updateIndicatorState() {
        if (autoSaveIndicator == null) return;
        
        if (idlePulse != null) {
            idlePulse.stop();
        }
        
        if (autoSaveEnabled && isEditable) {
            // Green pulsing - auto save enabled
            autoSaveIndicator.setStyle(
                "-fx-background-color: #38a169; " +
                "-fx-background-radius: 50%; " +
                "-fx-min-width: 8; -fx-max-width: 8; " +
                "-fx-min-height: 8; -fx-max-height: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(56,161,105,0.8), 8, 0.7, 0, 0);"
            );
            
            idlePulse = new FadeTransition(Duration.millis(1500), autoSaveIndicator);
            idlePulse.setFromValue(0.5);
            idlePulse.setToValue(1.0);
            idlePulse.setCycleCount(Timeline.INDEFINITE);
            idlePulse.setAutoReverse(true);
            idlePulse.play();
        } else {
            // Gray static - auto save disabled
            autoSaveIndicator.setStyle(
                "-fx-background-color: #cbd5e0; " +
                "-fx-background-radius: 50%; " +
                "-fx-min-width: 8; -fx-max-width: 8; " +
                "-fx-min-height: 8; -fx-max-height: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(203,213,224,0.4), 4, 0.5, 0, 0);"
            );
            autoSaveIndicator.setOpacity(1.0);
        }
    }



    private void loadGroups() {
        ObservableList<Group> groups = GroupService.getAllGroups();
        groupsFlowPane.getChildren().clear();
        for (Group group : groups) {
            groupsFlowPane.getChildren().add(createGroupCard(group));
        }
    }

    private VBox createGroupCard(Group group) {
        VBox card = new VBox(15);
        card.setPrefWidth(250);
        card.getStyleClass().add("student-card");
        card.setStyle(card.getStyle() + "; -fx-padding: 25;");

        Label nameLabel = new Label(group.getName());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        
        HBox countBox = new HBox(8);
        countBox.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web("#38a169"));
        Label countLabel = new Label(group.getStudentCount() + " o'quvchi");
        countLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14;");
        countBox.getChildren().addAll(dot, countLabel);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        Button newLessonBtn = new Button("Yangi dars");
        newLessonBtn.getStyleClass().addAll("btn", "btn-primary");
        newLessonBtn.setStyle("-fx-font-size: 12; -fx-padding: 5 10;");
        newLessonBtn.setMinWidth(100);
        newLessonBtn.setOnAction(e -> {
            Lesson newLesson = LessonService.createLesson(group.getId(), LocalDateTime.now());
            openLesson(group, newLesson, true, false);
        });

        Button historyBtn = new Button("Tarix");
        historyBtn.getStyleClass().add("btn");
        historyBtn.setStyle("-fx-font-size: 12; -fx-padding: 5 10; -fx-background-color: #edf2f7; -fx-text-fill: #4a5568;");
        historyBtn.setMinWidth(80);
        historyBtn.setOnAction(e -> showHistory(group));

        actionBox.getChildren().addAll(newLessonBtn, historyBtn);

        card.getChildren().addAll(nameLabel, countBox, actionBox);
        return card;
    }

    private void showHistory(Group group) {
        this.selectedGroup = group;
        historyGroupTitleLabel.setText(group.getName());
        loadHistory();
        groupSelectionView.setVisible(false);
        lessonHistoryView.setVisible(true);
    }

    private void loadHistory() {
        historyRowsContainer.getChildren().clear();
        ObservableList<Lesson> lessons = LessonService.getLessonsByGroup(selectedGroup.getId());
        
        if (lessons.isEmpty()) {
            Label emptyLabel = new Label("Ushbu guruhda darslar o'tilmagan");
            emptyLabel.setStyle("-fx-padding: 20; -fx-text-fill: #718096;");
            historyRowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Lesson lesson : lessons) {
            HBox row = new HBox(20);
            row.setUserData(lesson.getId());
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(15));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-cursor: hand;");
            row.setOnMouseClicked(e -> openLesson(selectedGroup, lesson, false, true));

            Label dateLabel = new Label(lesson.getLessonDate().format(DATETIME_FORMAT));
            dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #2d3748;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label arrow = new Label("→");
            arrow.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 18;");

            row.getChildren().addAll(dateLabel, spacer, arrow);
            historyRowsContainer.getChildren().add(row);
        }
    }

    private void openLesson(Group group, Lesson lesson, boolean editable, boolean fromHistoryView) {
        this.selectedGroup = group;
        this.currentLesson = lesson;
        this.isEditable = editable;
        this.fromHistory = fromHistoryView;
        this.isDeleteConfirmed = false;

        if (deleteContainer != null) deleteContainer.setVisible(false);
        if (deleteWarningLabel != null) deleteWarningLabel.setVisible(false);
        if (deleteButton != null) deleteButton.setText("Darsni o'chirish");

        if (autoSaveTimeline != null) {
            autoSaveTimeline.stop();
        }

        // Reset sort when opening a lesson
        this.currentSortField = "name";
        this.sortAscending = true;
        if (homeworkSortIcon != null) homeworkSortIcon.setText("↕");
        if (testScoreSortIcon != null) testScoreSortIcon.setText("↕");

        groupTitleLabel.setText(group.getName());
        dateLabel.setText("Sana: " + lesson.getLessonDate().format(DATETIME_FORMAT));
        
        enableEditButton.setVisible(!editable);
        saveButton.setVisible(editable);
        autoSaveToggle.setVisible(editable);
        autoSaveContainer.setVisible(editable);
        
        updateIndicatorState();

        this.currentDetails = LessonService.getLessonDetails(currentLesson.getId(), group.getId());
        renderLessonRows();
        
        groupSelectionView.setVisible(false);
        lessonHistoryView.setVisible(false);
        lessonDetailView.setVisible(true);
    }

    @FXML
    private void onSortHomework() {
        toggleSort("homework");
    }

    @FXML
    private void onSortTestScore() {
        toggleSort("test");
    }

    private void toggleSort(String field) {
        if (currentSortField.equals(field)) {
            sortAscending = !sortAscending;
        } else {
            currentSortField = field;
            sortAscending = true;
        }
        applySort();
        renderLessonRows();
    }

    private void applySort() {
        if (homeworkSortIcon == null || testScoreSortIcon == null) return;

        homeworkSortIcon.setText("↕");
        testScoreSortIcon.setText("↕");
        homeworkSortIcon.setStyle("-fx-text-fill: #a0aec0;");
        testScoreSortIcon.setStyle("-fx-text-fill: #a0aec0;");

        if (currentSortField.equals("homework")) {
            homeworkSortIcon.setText(sortAscending ? "↑" : "↓");
            homeworkSortIcon.setStyle("-fx-text-fill: #3182ce;");
            currentDetails.sort((a, b) -> {
                Double v1 = a.getHomeworkScore() == null ? 0.0 : a.getHomeworkScore();
                Double v2 = b.getHomeworkScore() == null ? 0.0 : b.getHomeworkScore();
                int res = sortAscending ? v1.compareTo(v2) : v2.compareTo(v1);
                if (res == 0) return a.getStudentName().compareTo(b.getStudentName());
                return res;
            });
        } else if (currentSortField.equals("test")) {
            testScoreSortIcon.setText(sortAscending ? "↑" : "↓");
            testScoreSortIcon.setStyle("-fx-text-fill: #3182ce;");
            currentDetails.sort((a, b) -> {
                Double v1 = a.getTestScore() == null ? 0.0 : a.getTestScore();
                Double v2 = b.getTestScore() == null ? 0.0 : b.getTestScore();
                int res = sortAscending ? v1.compareTo(v2) : v2.compareTo(v1);
                if (res == 0) return a.getStudentName().compareTo(b.getStudentName());
                return res;
            });
        } else if (currentSortField.equals("name")) {
            currentDetails.sort((a, b) -> {
                int res = sortAscending 
                    ? a.getStudentName().compareTo(b.getStudentName())
                    : b.getStudentName().compareTo(a.getStudentName());
                return res;
            });
        }
    }

    private void renderLessonRows() {
        lessonRowsContainer.getChildren().clear();
        if (currentDetails.isEmpty()) {
            Label emptyLabel = new Label("Ushbu guruhda o'quvchilar mavjud emas");
            emptyLabel.setStyle("-fx-padding: 20; -fx-text-fill: #718096;");
            lessonRowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (LessonDetail detail : currentDetails) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 15, 10, 15));
            row.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

            Label nameLabel = new Label(detail.getStudentName());
            nameLabel.setMinWidth(150);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            nameLabel.setWrapText(true);
            nameLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #2d3748;");

            TextField homeworkInput = createScoreInput(detail.getHomeworkScore(), "Ball");
            homeworkInput.setMinWidth(120);
            homeworkInput.setPrefWidth(120);
            homeworkInput.setMaxWidth(120);
            homeworkInput.setEditable(isEditable);
            homeworkInput.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    detail.setHomeworkScore(newVal.isEmpty() ? null : Double.parseDouble(newVal));
                    autoSaveIfEnabled();
                } catch (NumberFormatException ignored) {}
            });

            TextField testNameInput = new TextField(detail.getTestName());
            testNameInput.setPromptText("Tushgan test");
            testNameInput.setMinWidth(150);
            testNameInput.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(testNameInput, Priority.ALWAYS);
            testNameInput.setEditable(isEditable);
            testNameInput.getStyleClass().add("form-control");
            testNameInput.textProperty().addListener((obs, oldVal, newVal) -> {
                detail.setTestName(newVal);
                autoSaveIfEnabled();
            });

            TextField testScoreInput = createScoreInput(detail.getTestScore(), "Ball");
            testScoreInput.setMinWidth(120);
            testScoreInput.setPrefWidth(120);
            testScoreInput.setMaxWidth(120);
            testScoreInput.setEditable(isEditable);
            testScoreInput.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    detail.setTestScore(newVal.isEmpty() ? null : Double.parseDouble(newVal));
                    autoSaveIfEnabled();
                } catch (NumberFormatException ignored) {}
            });

            HBox checkContainer = new HBox();
            checkContainer.setMinWidth(80);
            checkContainer.setPrefWidth(80);
            checkContainer.setMaxWidth(80);
            checkContainer.setAlignment(Pos.CENTER);
            CheckBox attendanceCheck = new CheckBox();
            attendanceCheck.setSelected(detail.isAttendance());
            attendanceCheck.setDisable(!isEditable);
            
            boolean attended = detail.isAttendance();
            homeworkInput.setDisable(!attended || !isEditable);
            testNameInput.setDisable(!attended || !isEditable);
            testScoreInput.setDisable(!attended || !isEditable);
            
            attendanceCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                detail.setAttendance(newVal);
                homeworkInput.setDisable(!newVal || !isEditable);
                testNameInput.setDisable(!newVal || !isEditable);
                testScoreInput.setDisable(!newVal || !isEditable);
                autoSaveIfEnabled();
            });
            checkContainer.getChildren().add(attendanceCheck);

            row.getChildren().addAll(nameLabel, checkContainer, homeworkInput, testNameInput, testScoreInput);
            lessonRowsContainer.getChildren().add(row);
        }
    }

    private TextField createScoreInput(Double value, String prompt) {
        TextField input = new TextField();
        input.setPromptText(prompt);
        if (value != null && value != 0.0) {
            input.setText(String.valueOf(value));
        }
        input.getStyleClass().add("form-control");
        input.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 5;");
        return input;
    }

    @FXML
    private void onBackToGroups() {
        lessonDetailView.setVisible(false);
        lessonHistoryView.setVisible(false);
        groupSelectionView.setVisible(true);
        loadGroups();
    }

    @FXML
    private void onBackToHistoryOrGroups() {
        if (fromHistory) {
            lessonDetailView.setVisible(false);
            lessonHistoryView.setVisible(true);
            loadHistory();
        } else {
            onBackToGroups();
        }
    }

    @FXML
    private void onEnableEdit() {
        this.isEditable = true;
        enableEditButton.setVisible(false);
        saveButton.setVisible(true);
        autoSaveToggle.setVisible(true);
        autoSaveContainer.setVisible(true);
        if (fromHistory) {
            deleteContainer.setVisible(true);
        }
        updateIndicatorState();
        renderLessonRows();
    }

    @FXML
    private void onDeleteLesson() {
        if (!isDeleteConfirmed) {
            isDeleteConfirmed = true;
            deleteWarningLabel.setVisible(true);
            deleteButton.setText("Ha, o'chirilsin");
            return;
        }

        int lessonIdToDelete = currentLesson.getId();
        LessonService.deleteLesson(lessonIdToDelete);

        // Animation logic
        lessonDetailView.setVisible(false);
        lessonHistoryView.setVisible(true);

        // Find the row to animate in historyRowsContainer
        Node rowToAnimate = null;
        for (Node node : historyRowsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                // Since we store the ID or something in the row, but we don't currently.
                // We'll rely on the fact that loadHistory wasn't called yet, 
                // and we can find it by matching date if IDs aren't available.
                // Better: we can set a UserData on the row when creating it.
                if (Integer.valueOf(lessonIdToDelete).equals(row.getUserData())) {
                    rowToAnimate = row;
                    break;
                }
            }
        }

        if (rowToAnimate != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(500), rowToAnimate);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            Node finalRow = rowToAnimate;
            ft.setOnFinished(e -> {
                historyRowsContainer.getChildren().remove(finalRow);
                loadHistory(); // Refresh to be sure
            });
            ft.play();
        } else {
            loadHistory();
        }
    }

    @FXML
    private void onSaveLesson() {
        LessonService.saveAllLessonDetails(currentDetails);
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
