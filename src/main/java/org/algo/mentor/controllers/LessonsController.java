package org.algo.mentor.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.*;
import org.algo.mentor.services.GroupService;
import org.algo.mentor.services.LessonService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Darslar bo'limi uchun controller.
 * Yangi talablar bo'yicha dinamik UI va auto-save logikasi kiritilgan.
 */
public class LessonsController implements NavigableController {
    
    // FXML elementlari
    @FXML private VBox groupSelectionView;
    @FXML private FlowPane groupsFlowPane;
    @FXML private VBox lessonHistoryView;
    @FXML private Label historyGroupTitleLabel;
    @FXML private VBox historyRowsContainer;
    @FXML private VBox lessonDetailView;
    @FXML private Label groupTitleLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane attendanceFlowPane;
    @FXML private VBox homeworkSection;
    @FXML private FlowPane homeworkFlowPane;
    @FXML private Label homeworkCollapseIcon;
    @FXML private Button homeworkSortBtn;
    @FXML private Button homeworkHideBtn;
    @FXML private VBox testSection;
    @FXML private VBox testSessionsContainer;
    @FXML private Label testCollapseIcon;
    @FXML private VBox questionSection;
    @FXML private VBox questionSessionsContainer;
    @FXML private Label questionCollapseIcon;
    @FXML private Button deleteButton;
    @FXML private Button editButton;
    @FXML private HBox actionButtonsBar;

    // Ichki o'zgaruvchilar
    private NavigationController navigationController;
    private Group selectedGroup;
    private Lesson currentLesson;
    private List<Attendance> attendances = new ArrayList<>();
    private List<Homework> homeworks = new ArrayList<>();
    private List<TestSession> testSessions = new ArrayList<>();
    private List<QuestionSession> questionSessions = new ArrayList<>();
    
    private Timeline autoSaveTimeline;
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private boolean fromHistory = false;
    private boolean isEditing = true;

    @FXML
    public void initialize() {
        loadGroups();
        
        // Auto-save uchun taymer (debounce logikasi)
        autoSaveTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> performAutoSave()));
        autoSaveTimeline.setCycleCount(1);
    }

    /**
     * Guruhlarni yuklash
     */
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
        
        HBox countBox = new HBox(8);
        countBox.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web("#38a169"));
        Label countLabel = new Label(group.getStudentCount() + " o'quvchi");
        countLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #4a5568;");
        countBox.getChildren().addAll(countLabel, dot);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER);
        Button newLessonBtn = new Button("Yangi dars");
        newLessonBtn.getStyleClass().addAll("btn", "btn-primary");
        newLessonBtn.setOnAction(e -> {
            Lesson newLesson = LessonService.createLesson(group.getId(), LocalDateTime.now());
            openLesson(group, newLesson, false);
        });

        Button historyBtn = new Button("Tarix");
        historyBtn.getStyleClass().add("btn-history");
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
        for (Lesson lesson : lessons) {
            if (lesson.getLessonDate() == null) continue;
            HBox row = new HBox(20);
            row.setUserData(lesson.getId());
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(15));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-cursor: hand;");
            row.setOnMouseClicked(e -> openLesson(selectedGroup, lesson, true));

            Label dateTimeLbl = new Label(lesson.getLessonDate().format(DATETIME_FORMAT));
            dateTimeLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #2d3748;");
            
            VBox infoBox = new VBox(2);
            infoBox.getChildren().add(dateTimeLbl);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label arrow = new Label("→");

            row.getChildren().addAll(infoBox, spacer, arrow);
            historyRowsContainer.getChildren().add(row);
        }
    }

    /**
     * Dars oynasini ochish va ma'lumotlarni yuklash
     */
    private void openLesson(Group group, Lesson lesson, boolean fromHistoryView) {
        this.selectedGroup = group;
        this.currentLesson = lesson;
        this.fromHistory = fromHistoryView;
        this.isEditing = !fromHistoryView; // Tarixdan kirsa editing false, yangi dars bo'lsa true

        groupTitleLabel.setText(group.getName());
        dateLabel.setText(lesson.getLessonDate().format(DATETIME_FORMAT));
        
        updateEditingUI();
        
        // Ma'lumotlarni yuklash
        this.attendances = LessonService.getAttendances(lesson.getId(), group.getId());
        this.homeworks = LessonService.getHomeworks(lesson.getId(), group.getId());
        this.testSessions = LessonService.getTestSessions(lesson.getId(), group.getId());
        this.questionSessions = LessonService.getQuestionSessions(lesson.getId(), group.getId());

        // UI ni yangilash
        expandSection(homeworkFlowPane, homeworkCollapseIcon);
        expandSection(testSessionsContainer, testCollapseIcon);
        expandSection(questionSessionsContainer, questionCollapseIcon);

        homeworkSection.setVisible(!homeworks.isEmpty() && homeworks.stream().anyMatch(h -> h.getScore() > 0));
        homeworkSection.setManaged(homeworkSection.isVisible());
        
        renderAll();
        
        groupSelectionView.setVisible(false);
        lessonHistoryView.setVisible(false);
        lessonDetailView.setVisible(true);
    }

    private void expandSection(Node content, Label icon) {
        if (content != null) {
            content.setVisible(true);
            content.setManaged(true);
        }
        if (icon != null) {
            icon.setText("▲");
        }
    }

    private void updateEditingUI() {
        editButton.setVisible(fromHistory && !isEditing);
        editButton.setManaged(fromHistory && !isEditing);
        
        statusLabel.setVisible(isEditing);
        statusLabel.setManaged(isEditing);
        
        deleteButton.setVisible(fromHistory && isEditing);
        deleteButton.setManaged(fromHistory && isEditing);

        actionButtonsBar.setVisible(isEditing);
        actionButtonsBar.setManaged(isEditing);
        
        if (homeworkSortBtn != null) {
            homeworkSortBtn.setVisible(isEditing);
            homeworkSortBtn.setManaged(isEditing);
        }
        if (homeworkHideBtn != null) {
            homeworkHideBtn.setVisible(isEditing);
            homeworkHideBtn.setManaged(isEditing);
        }
    }

    @FXML
    private void onEditLesson() {
        this.isEditing = true;
        updateEditingUI();
        renderAll();
    }

    /**
     * Barcha bo'limlarni qayta chizish
     */
    private void renderAll() {
        refreshTotalScores();
        renderAttendance();
        renderHomework();
        renderTestSessions();
        renderQuestionSessions();
    }

    /**
     * Talabalarning umumiy ballarini qayta hisoblash
     */
    private void refreshTotalScores() {
        for (Attendance att : attendances) {
            double total = 0;
            int sid = att.getStudentId();
            
            // Vazifa balli
            total += homeworks.stream()
                    .filter(h -> h.getStudentId() == sid)
                    .mapToDouble(Homework::getScore)
                    .sum();
            
            // Test ballari
            for (TestSession ts : testSessions) {
                total += ts.getResults().stream()
                        .filter(r -> r.getStudentId() == sid)
                        .mapToDouble(TestResult::getTotalScore)
                        .sum();
            }
            
            // Savol ballari
            for (QuestionSession qs : questionSessions) {
                total += qs.getResults().stream()
                        .filter(r -> r.getStudentId() == sid)
                        .mapToDouble(QuestionResult::getTotalScore)
                        .sum();
            }
            att.setTotalScore(total);
        }
    }

    /**
     * 1. DAVOMATNI CHIZISH
     */
    private void renderAttendance() {
        attendanceFlowPane.getChildren().clear();
        // Kelmaganlarni oxiriga sortlash
        attendances.sort(Comparator.comparing(Attendance::isPresent).reversed().thenComparing(Attendance::getStudentName));

        for (Attendance att : attendances) {
            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(10));
            box.setPrefWidth(350);
            updateStudentBoxStyle(box, att.isPresent());

            Label name = new Label(att.getStudentName());
            System.out.println("Rendering attendance for: " + att.getStudentName());
            name.setPrefWidth(200);
            name.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");

            Label statusBtn = new Label(att.isPresent() ? "Keldi" : "Kelmadi");
            statusBtn.setPrefWidth(90);
            statusBtn.getStyleClass().add("status-badge");
            statusBtn.getStyleClass().add(att.isPresent() ? "status-present" : "status-absent");
            statusBtn.setCursor(isEditing ? Cursor.HAND : Cursor.DEFAULT);

            if (isEditing) {
                statusBtn.setOnMouseClicked(e -> {
                    att.setPresent(!att.isPresent());
                    triggerAutoSave();
                    renderAll();
                });
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            box.getChildren().addAll(name, statusBtn, spacer);
            attendanceFlowPane.getChildren().add(box);
        }
    }

    private void updateStudentBoxStyle(HBox box, boolean present) {
        if (present) {
            box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        } else {
            box.setStyle("-fx-background-color: #fff5f5; -fx-background-radius: 8; -fx-border-color: #feb2b2; -fx-border-radius: 8; -fx-opacity: 0.8;");
        }
    }

    /**
     * 2. VAZIFANI CHIZISH
     */
    private void renderHomework() {
        homeworkFlowPane.getChildren().clear();
        homeworks.sort((a, b) -> sortStudents(a.getStudentId(), b.getStudentId(), a.getScore(), b.getScore(), a.getStudentName(), b.getStudentName()));

        for (Homework hw : homeworks) {
            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(8));
            box.setPrefWidth(350);
            
            boolean present = isStudentPresent(hw.getStudentId());
            updateStudentBoxStyle(box, present);

            Label name = new Label(hw.getStudentName());
            name.setPrefWidth(200);
            name.setStyle("-fx-text-fill: #2d3748;");

            TextField scoreInput = new TextField(hw.getScore() > 0 ? String.valueOf(hw.getScore()) : "");
            scoreInput.setPromptText("Ball");
            scoreInput.getStyleClass().add("homework-input");
            scoreInput.setPrefWidth(80);
            scoreInput.setDisable(!present || !isEditing);
            scoreInput.textProperty().addListener((obs, old, newVal) -> {
                try {
                    hw.setScore(newVal.isEmpty() ? 0.0 : Double.parseDouble(newVal));
                    triggerAutoSave();
                } catch (NumberFormatException ignored) {}
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label hwTotal = new Label();
            hwTotal.getStyleClass().add("score-label");
            hwTotal.setPrefWidth(60);
            hwTotal.setAlignment(Pos.CENTER_RIGHT);
            hwTotal.textProperty().bind(hw.scoreProperty().asString("%.1f"));

            box.getChildren().addAll(name, scoreInput, spacer, hwTotal);
            homeworkFlowPane.getChildren().add(box);
        }
    }

    /**
     * 3. TEST SESSIYALARINI CHIZISH
     */
    private void renderTestSessions() {
        testSessionsContainer.getChildren().clear();
        testSection.setVisible(!testSessions.isEmpty());
        testSection.setManaged(testSection.isVisible());
        for (int i = 0; i < testSessions.size(); i++) {
            testSessionsContainer.getChildren().add(createSessionBlock(testSessions.get(i), "test", i + 1));
        }
    }

    /**
     * 4. SAVOL SESSIYALARINI CHIZISH
     */
    private void renderQuestionSessions() {
        questionSessionsContainer.getChildren().clear();
        questionSection.setVisible(!questionSessions.isEmpty());
        questionSection.setManaged(questionSection.isVisible());
        for (int i = 0; i < questionSessions.size(); i++) {
            questionSessionsContainer.getChildren().add(createSessionBlock(questionSessions.get(i), "question", i + 1));
        }
    }

    /**
     * Dinamik Blok (Sessiya) yaratish
     */
    private VBox createSessionBlock(Object sessionObj, String type, int index) {
        VBox card = new VBox(15);
        card.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 15 0;");
        
        String title = (type.equals("test") ? "Test-" : "Savol-") + index;
        String topic = type.equals("test") ? ((TestSession)sessionObj).getTopic() : ((QuestionSession)sessionObj).getTopic();
        double point = type.equals("test") ? ((TestSession)sessionObj).getPointPerCorrect() : ((QuestionSession)sessionObj).getPointPerCorrect();

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #1e293b;");
        
        TextField topicInput = new TextField(topic != null ? topic : "");
        topicInput.setPromptText("Mavzu");
        topicInput.getStyleClass().add("group-topic-field");
        topicInput.setPrefWidth(300);
        topicInput.setDisable(!isEditing);
        topicInput.textProperty().addListener((obs, old, newVal) -> {
            if (type.equals("test")) ((TestSession)sessionObj).setTopic(newVal);
            else ((QuestionSession)sessionObj).setTopic(newVal);
            triggerAutoSave();
        });

        Label pointLbl = new Label("1 ta to'g'ri =");
        TextField pointInput = new TextField(String.valueOf(point));
        pointInput.getStyleClass().add("input-small");
        pointInput.setPrefWidth(70);
        pointInput.setDisable(!isEditing);
        pointInput.textProperty().addListener((obs, old, newVal) -> {
            try {
                double p = newVal.isEmpty() ? 0.0 : Double.parseDouble(newVal);
                if (type.equals("test")) ((TestSession)sessionObj).setPointPerCorrect(p);
                else ((QuestionSession)sessionObj).setPointPerCorrect(p);
                recalculateSession(sessionObj, type);
                triggerAutoSave();
            } catch (Exception ignored) {}
        });

        header.getChildren().addAll(titleLbl, topicInput, pointLbl, pointInput);

        if (isEditing) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button sortBtn = new Button("⇅ Tartiblash");
            sortBtn.getStyleClass().add("btn-sort");
            sortBtn.setOnAction(e -> {
                if (type.equals("test")) {
                    ((TestSession)sessionObj).getResults().sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));
                    renderTestSessions();
                } else {
                    ((QuestionSession)sessionObj).getResults().sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));
                    renderQuestionSessions();
                }
            });

            Button delBtn = new Button("✕");
            delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
            delBtn.setOnAction(e -> removeSessionWithAnimation(card, sessionObj, type));
            
            header.getChildren().addAll(spacer, sortBtn, new Region() {{ setPrefWidth(10); }}, delBtn);
        }
        
        FlowPane resultsPane = new FlowPane(15, 15);
        List<?> results = type.equals("test") ? ((TestSession)sessionObj).getResults() : ((QuestionSession)sessionObj).getResults();
        
        // Sortlash: Kelmaganlar oxiriga
        ((List)results).sort((a, b) -> {
            int s1 = type.equals("test") ? ((TestResult)a).getStudentId() : ((QuestionResult)a).getStudentId();
            int s2 = type.equals("test") ? ((TestResult)b).getStudentId() : ((QuestionResult)b).getStudentId();
            boolean p1 = isStudentPresent(s1);
            boolean p2 = isStudentPresent(s2);
            if (p1 != p2) return p1 ? -1 : 1;
            return 0;
        });

        for (Object res : results) {
            resultsPane.getChildren().add(createResultRow(res, type, sessionObj));
        }

        card.getChildren().addAll(header, resultsPane);
        return card;
    }

    private HBox createResultRow(Object res, String type, Object session) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8));
        box.setPrefWidth(500);
        
        int studentId = type.equals("test") ? ((TestResult)res).getStudentId() : ((QuestionResult)res).getStudentId();
        String nameStr = type.equals("test") ? ((TestResult)res).getStudentName() : ((QuestionResult)res).getStudentName();
        boolean present = isStudentPresent(studentId);
        updateStudentBoxStyle(box, present);

        Label name = new Label(nameStr);
        name.setPrefWidth(150);
        name.setStyle("-fx-font-size: 13; -fx-text-fill: #2d3748;");

        TextField secInput = new TextField(type.equals("test") ? ((TestResult)res).getSection() : ((QuestionResult)res).getSection());
        secInput.setPromptText("Variant");
        secInput.getStyleClass().add("input-medium");
        secInput.setPrefWidth(120);
        secInput.setDisable(!present || !isEditing);
        secInput.textProperty().addListener((obs, old, newVal) -> {
            if (type.equals("test")) ((TestResult)res).setSection(newVal);
            else ((QuestionResult)res).setSection(newVal);
            triggerAutoSave();
        });

        // Counter (+ / -)
        HBox counter = new HBox(0);
        counter.setAlignment(Pos.CENTER);
        counter.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");
        
        Button minus = new Button("-");
        minus.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        
        TextField countVal = new TextField(String.valueOf(type.equals("test") ? ((TestResult)res).getCorrectCount() : ((QuestionResult)res).getCorrectCount()));
        countVal.setPrefWidth(40);
        countVal.setAlignment(Pos.CENTER);
        countVal.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 1; -fx-padding: 5 0;");
        countVal.setDisable(!isEditing);
        
        Button plus = new Button("+");
        plus.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        
        minus.setOnAction(e -> {
            try {
                int c = Integer.parseInt(countVal.getText());
                if (c > 0) updateCount(res, type, session, c - 1, countVal);
            } catch (Exception ignored) {}
        });
        plus.setOnAction(e -> {
            try {
                int c = Integer.parseInt(countVal.getText());
                updateCount(res, type, session, c + 1, countVal);
            } catch (Exception ignored) {}
        });
        countVal.textProperty().addListener((obs, old, newVal) -> {
            try {
                if (!newVal.isEmpty()) {
                    int c = Integer.parseInt(newVal);
                    updateCount(res, type, session, c, null);
                }
            } catch (Exception ignored) {}
        });

        counter.getChildren().addAll(minus, countVal, plus);
        counter.setDisable(!present || !isEditing);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label total = new Label();
        total.getStyleClass().add("score-label");
        total.setPrefWidth(60);
        total.setAlignment(Pos.CENTER_RIGHT);
        if (type.equals("test")) total.textProperty().bind(((TestResult)res).totalScoreProperty().asString("%.1f"));
        else total.textProperty().bind(((QuestionResult)res).totalScoreProperty().asString("%.1f"));

        box.getChildren().addAll(name, secInput, counter, spacer, total);
        return box;
    }

    private void updateCount(Object res, String type, Object session, int newCount, TextField field) {
        if (field != null) field.setText(String.valueOf(newCount));
        double point = type.equals("test") ? ((TestSession)session).getPointPerCorrect() : ((QuestionSession)session).getPointPerCorrect();
        if (type.equals("test")) {
            ((TestResult)res).setCorrectCount(newCount);
            ((TestResult)res).setTotalScore(newCount * point);
        } else {
            ((QuestionResult)res).setCorrectCount(newCount);
            ((QuestionResult)res).setTotalScore(newCount * point);
        }
        triggerAutoSave();
    }

    private void recalculateSession(Object session, String type) {
        double point = type.equals("test") ? ((TestSession)session).getPointPerCorrect() : ((QuestionSession)session).getPointPerCorrect();
        List<?> results = type.equals("test") ? ((TestSession)session).getResults() : ((QuestionSession)session).getResults();
        for (Object r : results) {
            if (type.equals("test")) ((TestResult)r).setTotalScore(((TestResult)r).getCorrectCount() * point);
            else ((QuestionResult)r).setTotalScore(((QuestionResult)r).getCorrectCount() * point);
        }
    }

    /**
     * Yangi Test qo'shish
     */
    @FXML
    private void onAddNewTest() {
        TestSession newSession = LessonService.createTestSession(currentLesson.getId());
        if (newSession != null) {
            // Service yangi sessiya yaratganda unga barcha studentlarni ham qo'shib qo'yishi kerak.
            // getTestSessions chaqirilganda sync logikasi borligi sababli barcha studentlar qo'shiladi.
            this.testSessions = LessonService.getTestSessions(currentLesson.getId(), selectedGroup.getId());
            renderTestSessions();
        }
    }

    /**
     * Yangi Savol qo'shish
     */
    @FXML
    private void onAddNewQuestion() {
        QuestionSession newSession = LessonService.createQuestionSession(currentLesson.getId());
        if (newSession != null) {
            this.questionSessions = LessonService.getQuestionSessions(currentLesson.getId(), selectedGroup.getId());
            renderQuestionSessions();
        }
    }

    @FXML
    private void onSortHomework() {
        homeworks.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        renderHomework();
    }

    @FXML private void onShowHomework() { 
        homeworkSection.setOpacity(1.0); 
        homeworkSection.setVisible(true); 
        homeworkSection.setManaged(true); 
        homeworkFlowPane.setVisible(true);
        homeworkFlowPane.setManaged(true);
        homeworkCollapseIcon.setText("▲");
        renderHomework(); 
    }
    
    @FXML private void onToggleHomework() {
        boolean visible = !homeworkFlowPane.isVisible();
        homeworkFlowPane.setVisible(visible);
        homeworkFlowPane.setManaged(visible);
        homeworkCollapseIcon.setText(visible ? "▲" : "▼");
    }

    @FXML private void onToggleTest() {
        boolean visible = !testSessionsContainer.isVisible();
        testSessionsContainer.setVisible(visible);
        testSessionsContainer.setManaged(visible);
        testCollapseIcon.setText(visible ? "▲" : "▼");
    }

    @FXML private void onToggleQuestion() {
        boolean visible = !questionSessionsContainer.isVisible();
        questionSessionsContainer.setVisible(visible);
        questionSessionsContainer.setManaged(visible);
        questionCollapseIcon.setText(visible ? "▲" : "▼");
    }

    @FXML private void onHideHomework() {
        // Vazifa ballarini darxol nolga tushirish
        for (Homework hw : homeworks) {
            hw.setScore(0.0);
            hw.setNote("");
        }
        triggerAutoSave();
        renderAll();

        FadeTransition ft = new FadeTransition(Duration.millis(300), homeworkSection);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            homeworkSection.setVisible(false);
            homeworkSection.setManaged(false);
        });
        ft.play();
    }

    /**
     * Avto-saqlashni boshlash
     */
    private void triggerAutoSave() {
        refreshTotalScores();
        statusLabel.setText("Saqlanmoqda...");
        statusLabel.setStyle("-fx-text-fill: #f59e0b;");
        autoSaveTimeline.playFromStart();
    }

    /**
     * Ma'lumotlarni DB ga yozish
     */
    private void performAutoSave() {
        LessonService.saveAllData(attendances, homeworks, testSessions, questionSessions);
        
        Platform.runLater(() -> {
            statusLabel.setText("Barcha o'zgarishlar saqlandi");
            statusLabel.setStyle("-fx-text-fill: #10b981;");
        });
    }

    private boolean isStudentPresent(int studentId) {
        return attendances.stream().filter(a -> a.getStudentId() == studentId).findFirst().map(Attendance::isPresent).orElse(true);
    }

    private int sortStudents(int id1, int id2, double s1, double s2, String n1, String n2) {
        boolean p1 = isStudentPresent(id1);
        boolean p2 = isStudentPresent(id2);
        if (p1 != p2) return p1 ? -1 : 1;
        if (s1 != s2) return Double.compare(s2, s1); // Ball bo'yicha kamayish
        return n1.compareTo(n2);
    }

    @FXML private void onBackToGroups() {
        performAutoSave(); // Chiqishdan oldin saqlash
        lessonDetailView.setVisible(false);
        lessonHistoryView.setVisible(false);
        groupSelectionView.setVisible(true);
        loadGroups();
    }
    @FXML private void onBackToHistoryOrGroups() {
        performAutoSave(); // Chiqishdan oldin saqlash
        if (fromHistory) { lessonDetailView.setVisible(false); lessonHistoryView.setVisible(true); loadHistory(); }
        else onBackToGroups();
    }

    private void removeSessionWithAnimation(VBox card, Object sessionObj, String type) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), card);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            if (type.equals("test")) {
                TestSession ts = (TestSession) sessionObj;
                LessonService.deleteTestSession(ts.getId());
                testSessions.remove(ts);
                renderTestSessions();
            } else {
                QuestionSession qs = (QuestionSession) sessionObj;
                LessonService.deleteQuestionSession(qs.getId());
                questionSessions.remove(qs);
                renderQuestionSessions();
            }
            triggerAutoSave();
        });
        ft.play();
    }

    @FXML
    private void onDeleteLesson() {
        LessonService.deleteLesson(currentLesson.getId());
        onBackToHistoryOrGroups();
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
