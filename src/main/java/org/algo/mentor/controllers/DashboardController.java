package org.algo.mentor.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.services.ReportService;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController implements NavigableController {
    @FXML private VBox contentVBox;
    @FXML private Label studentCountLabel;
    @FXML private Label groupCountLabel;
    @FXML private Label lessonCountLabel;
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private VBox upcomingLessonsVBox;
    @FXML private VBox riskListVBox;

    private NavigationController navigationController;
    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        startClock();
        loadStats();
        loadUpcomingLessons();
        loadRiskList();
    }

    private void startClock() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d-MMMM, yyyy");
        ZoneId tashkentZone = ZoneId.of("Asia/Tashkent");

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            ZonedDateTime now = ZonedDateTime.now(tashkentZone);
            timeLabel.setText(now.format(timeFormatter));
            dateLabel.setText(now.format(dateFormatter));
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();

        // Stop clock when navigating away
        timeLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && clockTimeline != null) {
                clockTimeline.stop();
            }
        });
    }

    private void loadStats() {
        ReportService.SummaryStat summary = ReportService.getSummaryStatistics();
        studentCountLabel.setText(String.valueOf(summary.totalStudents()));
        groupCountLabel.setText(String.valueOf(summary.totalGroups()));
        lessonCountLabel.setText(String.valueOf(summary.lessonsToday()));
    }

    private void loadUpcomingLessons() {
        upcomingLessonsVBox.getChildren().clear();
        List<ReportService.UpcomingLesson> lessons = ReportService.getUpcomingLessons();
        
        if (lessons.isEmpty()) {
            Label placeholder = new Label("Bugun darslar mavjud emas");
            placeholder.setStyle("-fx-text-fill: #a0aec0; -fx-font-style: italic;");
            upcomingLessonsVBox.getChildren().add(placeholder);
            return;
        }

        for (ReportService.UpcomingLesson lesson : lessons) {
            HBox row = new HBox(15);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 12; -fx-background-color: #f7fafc; -fx-background-radius: 8; -fx-border-color: #edf2f7; -fx-border-radius: 8;");
            
            VBox timeBox = new VBox(2);
            timeBox.setAlignment(javafx.geometry.Pos.CENTER);
            timeBox.setMinWidth(70);
            Label timeLbl = new Label(lesson.time());
            timeLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 15;");
            timeBox.getChildren().add(timeLbl);
            
            VBox infoBox = new VBox(2);
            Label groupLbl = new Label(lesson.groupName());
            groupLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
            infoBox.getChildren().addAll(groupLbl);
            
            HBox.setHgrow(infoBox, Priority.ALWAYS);
            row.getChildren().addAll(timeBox, infoBox);
            upcomingLessonsVBox.getChildren().add(row);
        }
    }

    private void loadRiskList() {
        riskListVBox.getChildren().clear();
        List<ReportService.RiskStudent> students = ReportService.getAtRiskStudents();

        if (students.isEmpty()) {
            Label placeholder = new Label("Hozircha hamma o'quvchilar ko'rsatkichlari yaxshi");
            placeholder.setStyle("-fx-text-fill: #a0aec0; -fx-font-style: italic;");
            riskListVBox.getChildren().add(placeholder);
            return;
        }

        for (ReportService.RiskStudent student : students) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10; -fx-background-color: #fff5f5; -fx-background-radius: 8; -fx-border-color: #fed7d7; -fx-border-radius: 8;");

            VBox infoBox = new VBox(2);
            Label nameLbl = new Label(student.fullName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #c53030;");
            Label groupLbl = new Label(student.groupName());
            groupLbl.setStyle("-fx-text-fill: #718096; -fx-font-size: 11;");
            infoBox.getChildren().addAll(nameLbl, groupLbl);

            VBox statBox = new VBox(2);
            statBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            Label attLbl = new Label(String.format("%.0f%%", student.missedRate()));
            attLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (student.missedRate() > 30 ? "#e53e3e" : "#dd6b20") + ";");
            Label scoreLbl = new Label(String.format("%.1f ball", student.avgScore()));
            scoreLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #718096;");
            statBox.getChildren().addAll(attLbl, scoreLbl);

            HBox.setHgrow(infoBox, Priority.ALWAYS);
            row.getChildren().addAll(infoBox, statBox);
            riskListVBox.getChildren().add(row);
        }
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
