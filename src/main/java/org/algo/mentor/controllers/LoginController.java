package org.algo.mentor.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.stage.DirectoryChooser;
import org.algo.mentor.config.AppDirectoryManager;
import org.algo.mentor.config.DatabaseManager;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.services.AuthService;
import org.algo.mentor.models.User;

import java.io.File;
import java.nio.file.Path;

public class LoginController implements NavigableController {
    @FXML private StackPane rootPane;
    @FXML private VBox loginCard;
    @FXML private TextField dbPathField;
    @FXML private Label dbStatusLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private AuthService authService;
    private NavigationController navigationController;
    private boolean isDatabaseReady = false;

    @FXML
    public void initialize() {
        authService = new AuthService();
        applyAnimations();
        setupHoverEffects();

        // Default folder holatini tekshirish
        Path defaultPath = AppDirectoryManager.getAppDirectory();
        if (defaultPath != null) {
            dbPathField.setText(defaultPath.toString());
            checkDatabaseStatus(defaultPath);
        } else {
            loginButton.setDisable(true);
            loginButton.setOpacity(0.5);
        }
    }

    private void checkDatabaseStatus(Path path) {
        if (AppDirectoryManager.checkDatabaseExists(path)) {
            dbStatusLabel.setText("Ma'lumotlar bazasi topildi!");
            dbStatusLabel.setStyle("-fx-text-fill: #38a169; -fx-font-size: 11; -fx-font-weight: bold;");
            
            AppDirectoryManager.setAppDirectory(path);
            DatabaseManager.reinitialize();
            
            isDatabaseReady = true;
            loginButton.setDisable(false);
            loginButton.setOpacity(1.0);
        } else {
            dbStatusLabel.setText("database.db fayli topilmadi!");
            dbStatusLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 11; -fx-font-weight: bold;");
            
            isDatabaseReady = false;
            loginButton.setDisable(true);
            loginButton.setOpacity(0.5);
        }
    }

    @FXML
    protected void onSelectFolderButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Ma'lumotlar bazasi papkasini tanlang");
        
        File selectedDirectory = directoryChooser.showDialog(rootPane.getScene().getWindow());
        
        if (selectedDirectory != null) {
            Path path = selectedDirectory.toPath();
            dbPathField.setText(path.toString());
            checkDatabaseStatus(path);
        }
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    @FXML
    protected void onLoginButtonClick() {
        if (!isDatabaseReady) {
            showError("Avval ma'lumotlar bazasini tanlang!");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Login yoki parol bo'sh bo'lmasligi kerak");
            return;
        }

        User user = authService.authenticate(username, password);
        if (user != null) {
            navigationController.setCurrentUser(user);
            navigationController.navigateTo("dashboard-view.fxml", "DashboardController");
        } else {
            showError("Login yoki parol noto'g'ri!");
            passwordField.clear();
        }
    }

    private void applyAnimations() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), loginCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition moveUp = new TranslateTransition(Duration.millis(800), loginCard);
        moveUp.setFromY(30);
        moveUp.setToY(0);

        ParallelTransition pt = new ParallelTransition(fadeIn, moveUp);
        pt.play();
    }

    private void setupHoverEffects() {
        String normalStyle = "-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;";
        
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(hoverStyle));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(normalStyle));
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginCard);
        shake.setByX(10);
        shake.setAutoReverse(true);
        shake.setCycleCount(4);
        shake.play();
        shake.setOnFinished(e -> loginCard.setTranslateX(0));
    }
}
