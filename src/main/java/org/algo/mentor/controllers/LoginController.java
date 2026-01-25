package org.algo.mentor.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.algo.mentor.core.NavigableController;
import org.algo.mentor.core.NavigationController;
import org.algo.mentor.services.AuthService;
import org.algo.mentor.models.User;

public class LoginController implements NavigableController {
    @FXML private StackPane rootPane;
    @FXML private VBox loginCard;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private AuthService authService;
    private NavigationController navigationController;

    @FXML
    public void initialize() {
        authService = new AuthService();
        applyAnimations();
        setupHoverEffects();
    }

    private void applyAnimations() {
        // Fade in and Slide up login card
        loginCard.setOpacity(0);
        loginCard.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(800), loginCard);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(800), loginCard);
        slide.setToY(0);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.play();
    }

    private void setupHoverEffects() {
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;");
        });
        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12; -fx-cursor: hand;");
        });
    }

    @Override
    public void initialize(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    @FXML
    protected void onLoginButtonClick() {
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
