package org.algo.mentor.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.models.User;

public class MainController {
    public BorderPane mainLayout;
    public HBox headerHBox;
    public VBox sidebarVBox;
    public StackPane contentArea;

    private NavigationController navigationController;

    public void initialize() {
        navigationController = NavigationController.getInstance();
        navigationController.setMainLayout(mainLayout);
        navigationController.setMainController(this);
        
        setupHeader();
        setupSidebar();
        hideSidebar();
        hideHeader();
        navigationController.navigateTo("login-view.fxml", "LoginController");
    }

    private void setupHeader() {
        headerHBox.setStyle("-fx-background-color: white; -fx-padding: 0 30; -fx-border-color: #edf2f7; -fx-border-width: 0 0 1 0;");
        headerHBox.setPrefHeight(65);
        headerHBox.setAlignment(Pos.CENTER_RIGHT);
    }

    public void updateHeader(User user) {
        headerHBox.getChildren().clear();
        if (user == null) return;

        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        Label userLabel = new Label(user.getFullName());
        userLabel.setStyle("-fx-text-fill: #2d3748; -fx-font-size: 14; -fx-font-weight: bold;");

        StackPane avatarBox = new StackPane();
        avatarBox.setMinSize(40, 40);
        avatarBox.setMaxSize(40, 40);
        avatarBox.setPrefSize(40, 40);
        avatarBox.setStyle("-fx-background-color: #ebf8ff; -fx-background-radius: 50%;");

        Label initials = new Label(user.getAvatar());
        initials.setStyle("-fx-text-fill: #3182ce; -fx-font-size: 14; -fx-font-weight: bold;");
        avatarBox.getChildren().add(initials);

        userBox.getChildren().addAll(userLabel, avatarBox);
        headerHBox.getChildren().add(userBox);
    }

    private void setupSidebar() {
        sidebarVBox.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-border-color: #edf2f7; -fx-border-width: 0 1 0 0;");
        sidebarVBox.setPrefWidth(240);

        Label titleLabel = new Label("Mentor");
        titleLabel.setStyle("-fx-text-fill: #2d3748; -fx-font-size: 24; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(30, 0, 30, 30));

        VBox menuVBox = new VBox(5);
        menuVBox.setPadding(new Insets(0, 15, 0, 15));

        String[][] menuItems = {
            {"Asosiy", "dashboard-view.fxml", "DashboardController"},
            {"O'quvchilar", "students-view.fxml", "StudentsController"},
            {"Guruhlar", "groups-view.fxml", "GroupsController"},
            {"Darslar", "lessons-view.fxml", "LessonsController"},
            {"Hisobotlar", "reports-view.fxml", "ReportsController"},
            {"Dars jadvali", "schedule-view.fxml", "ScheduleController"}
        };

        for (String[] item : menuItems) {
            Button menuBtn = new Button(item[0]);
            menuBtn.setPrefWidth(Double.MAX_VALUE);
            menuBtn.setAlignment(Pos.CENTER_LEFT);
            menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #718096; -fx-padding: 12 15; -fx-font-size: 14; -fx-cursor: hand; -fx-background-radius: 8;");
            
            menuBtn.setOnMouseEntered(e -> menuBtn.setStyle("-fx-background-color: #f7fafc; -fx-text-fill: #3182ce; -fx-padding: 12 15; -fx-font-size: 14; -fx-cursor: hand; -fx-background-radius: 8;"));
            menuBtn.setOnMouseExited(e -> menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #718096; -fx-padding: 12 15; -fx-font-size: 14; -fx-cursor: hand; -fx-background-radius: 8;"));
            
            menuBtn.setOnAction(e -> navigationController.navigateTo(item[1], item[2]));
            menuVBox.getChildren().add(menuBtn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Chiqish");
        logoutBtn.setPrefWidth(Double.MAX_VALUE);
        String logoutNormalStyle = "-fx-background-color: #fff5f5; -fx-text-fill: #e53e3e; -fx-padding: 12 20; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 0; -fx-cursor: hand;";
        String logoutHoverStyle = "-fx-background-color: #feb2b2; -fx-text-fill: #c53030; -fx-padding: 12 20; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 0; -fx-cursor: hand;";
        
        logoutBtn.setStyle(logoutNormalStyle);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logoutHoverStyle));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(logoutNormalStyle));
        
        logoutBtn.setOnAction(e -> navigationController.logout());

        sidebarVBox.getChildren().addAll(titleLabel, menuVBox, spacer, logoutBtn);
    }

    private void handleMenuClick(String menuItem) {
        if (menuItem.equals("Guruxlar")) {
            navigationController.navigateTo("groups-view.fxml", "GroupsController");
        } else if (menuItem.equals("O'quvchilarni boshqarish")) {
            navigationController.navigateTo("students-view.fxml", "StudentsController");
        } else if (menuItem.equals("Hisobotlar")) {
            navigationController.navigateTo("reports-view.fxml", "ReportsController");
        } else {
            navigationController.navigateTo("dashboard-view.fxml", "DashboardController");
        }
    }

    public void showSidebar() {
        sidebarVBox.setVisible(true);
        sidebarVBox.setManaged(true);
    }

    public void hideSidebar() {
        sidebarVBox.setVisible(false);
        sidebarVBox.setManaged(false);
    }

    public void showHeader() {
        headerHBox.setVisible(true);
        headerHBox.setManaged(true);
    }

    public void hideHeader() {
        headerHBox.setVisible(false);
        headerHBox.setManaged(false);
    }
}
