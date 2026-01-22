package org.algo.mentor.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.algo.mentor.HelloApplication;
import org.algo.mentor.controllers.MainController;
import org.algo.mentor.models.User;

import java.io.IOException;

public class NavigationController {
    private static NavigationController instance;
    private BorderPane mainLayout;
    private MainController mainController;
    private User currentUser;

    private NavigationController() {
    }

    public static NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }

    public void setMainLayout(BorderPane mainLayout) {
        this.mainLayout = mainLayout;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (mainController != null && user != null) {
            mainController.updateHeader(user);
            mainController.showHeader();
            mainController.showSidebar();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void navigateTo(String fxmlFileName, String controllerName) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/" + fxmlFileName));
            Parent view = loader.load();
            mainLayout.setCenter(view);

            Object controller = loader.getController();
            if (controller instanceof NavigableController) {
                ((NavigableController) controller).initialize(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        currentUser = null;
        if (mainController != null) {
            mainController.updateHeader(null);
            mainController.hideSidebar();
        }
        navigateTo("login-view.fxml", "LoginController");
    }
}
